package de.eblaas.museum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.MAX_VALUE;


/**
 * MET objects are mapped in a 4 dimensional space containing [height, width, depth, weight].
 * <p>
 * Not every objects has all 4 dimensions, but at leased  [height, width, -1, -1]. Missing dimensions are set to -1.
 * <p>
 * Unit of height, width, depth = cm, Unit of weight = g
 * </p>
 */
@ToString
@Getter
@AllArgsConstructor
public class Dimension {

    final double height, width, depth, weight;

    /**
     * Check if a MET object represented by this dimension object fits into the specified dimension boundaries.
     *
     * <p> IMPORTANT: NOT every object has ALL dimensions </p>
     * <p> min boundaries, require the dimension to be available, else considered as not fitting. <p>
     * <p> max boundaries, don't require the dimension to be available, only limiting it if present. </p>
     */
    boolean doesItFit(DimensionBoundary boundary) {
        return (height >= boundary.minHeight && height <= boundary.maxHeight) &&
                (width >= boundary.minWidth && width <= boundary.maxWidth) &&
                (depth >= boundary.minDepth && depth <= boundary.maxDepth) &&
                (weight >= boundary.minWeight && weight <= boundary.maxWeight);
    }


    final static Pattern H_W_D_W_PATTERN = Pattern.compile(
            ".*?\\(((?<g1>[\\d|\\.]+)\\sx\\s)?((?<g2>[\\d|\\.]+)\\sx\\s)?((?<g3>[\\d|\\.]+)\\s?(?<dimunit>cm|in.|mm))(,.(?<weight>[\\d|\\.]+).(?<unit>(\\w)*))?.?\\).*");
    final static Pattern WEIGHT_PATTERN = Pattern.compile(".*?\\(([\\d|\\.]+).*?g\\)");

    final static String RANGE_PATTERN = "\\(([\\d|\\.]+)-([\\d|\\.]+)";
    final static String MISSING_UNIT_PATTERN = "\\(([\\d|\\.]+)\\)";
    final static String SEPARATE_VALUES_PATTERN = "\\((\\d+\\.\\d)(\\d+\\.\\d)";
    final static String VALUES_PATTERN = "\\((\\d+\\.)\\s?(\\d)\\.?";

    static Dimension fromString(String dimensionRaw) {

        // data cleaning to bring it in a format like (H x W x D cm)
        var cleaned = dimensionRaw
                // replace recurring format errors
                .replaceAll("×|x|X", "x")
                .replace("–", "-")
                .replace("  x", " x")
                .replace(" x. ", " x ")
                .replace("cm Diam.", "cm")
                .replace("cm x", "x")
                .replace("cm H.", "cm")
                .replace("cm.", "cm")
                .replace("com", "cm")
                .replace("  cm", " cm")
                .replace("..", ".")
                .replace("( ", "(")
                .replace("))", ")")
                // value ranges are transformed into upper limit. e.g. (12.1-25.4)=>(25.4)
                .replaceAll(RANGE_PATTERN, "($2")
                // missing units are assumed to be cm. e.g. (12.1)=>(12.1 cm)
                .replaceAll(MISSING_UNIT_PATTERN, "($1 cm)")
                // split dimensions after first decimal point e.g. (12.456.3 cm)=>(12.4 x 56.3 cm)
                .replaceAll(SEPARATE_VALUES_PATTERN, "($1 x $2")
                // remove spaces between decimal point e.g. 12. 3 cm => 12.3 cm
                .replaceAll(VALUES_PATTERN, "($1$2")
                .trim();

        Matcher m;
        try {
            // MET objects may consist of multiple items, they get recursively parsed and combined into a single dimension
            // object, where the maximum of each dimension is used
            if (cleaned.split("[;|\n]").length > 1) {
                var dim = Arrays.stream(cleaned.split("[;|\n]"))
                        .map(String::trim)
                        // create new dimension for each entry
                        .map(Dimension::fromString)
                        .filter(Objects::nonNull)
                        .reduce(
                                (d1, d2) -> new Dimension(
                                        Math.max(d1.height, d2.height),
                                        Math.max(d1.width, d2.width),
                                        Math.max(d1.depth, d2.depth),
                                        Math.max(d1.weight, d2.weight)));

                if (dim.isPresent()) {
                    return dim.get();
                }

                // match the cleaned data, if matches extract dimension values and normalize it to cm and g
            } else if ((m = H_W_D_W_PATTERN.matcher(cleaned)).matches()) {
                var g1 = m.group("g1");
                var g2 = m.group("g2");
                var g3 = m.group("g3");
                var weightStr = m.group("weight");
                var weightUnit = m.group("unit");
                var dimUnit = m.group("dimunit");

                double groupMultiplier = 1;

                if (dimUnit.equals("in.")) {
                    groupMultiplier = 2.54;
                }
                if (dimUnit.equals("mm")) {
                    groupMultiplier = 0.1;
                }

                double height, width, depth = -1, weight = -1;

                // single dimension (e.g diam)
                if (g1 == null && g2 == null) {
                    height = width = Double.parseDouble(g3) * groupMultiplier;

                    // two dimensions available H x L
                } else if (g2 == null) {
                    height = Double.parseDouble(g1) * groupMultiplier;
                    width = Double.parseDouble(g3) * groupMultiplier;

                    // three dimensions H x L x D
                } else {
                    height = Double.parseDouble(g1) * groupMultiplier;
                    width = Double.parseDouble(g2) * groupMultiplier;
                    depth = Double.parseDouble(g3) * groupMultiplier;
                }

                // check if weight is present
                if (weightStr != null && weightUnit != null) {
                    double multiplier = 1;
                    if (weightUnit.equals("kg")) {
                        multiplier = 1000;
                    }
                    if (weightUnit.equals("dwt")) {
                        multiplier = 1.555;
                    }
                    if (weightUnit.equals("oz")) {
                        multiplier = 28.35;
                    }
                    weight = Double.parseDouble(weightStr) * multiplier;
                }
                return new Dimension(height, width, depth, weight);

                // match a weight entry
            } else if ((m = WEIGHT_PATTERN.matcher(cleaned)).matches()) {
                var weight = Double.parseDouble(m.group(1));
                return new Dimension(-1, -1, -1, weight);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }


    /**
     * DimensionBoundary describes the lower and upper limit for [height, width, depth, weight]
     */
    @ToString
    @Getter
    @Setter
    public static class DimensionBoundary {

        Double minHeight = -1.0, minWidth = -1.0, minDepth = -1.0, minWeight = -1.0;
        Double maxHeight = MAX_VALUE, maxWidth = MAX_VALUE, maxDepth = MAX_VALUE, maxWeight = MAX_VALUE;

        Double[] args() {
            return new Double[]{minHeight, maxHeight, minWidth, maxWidth, minDepth, maxDepth, minWeight, maxWeight};
        }
    }

    @ToString
    @Getter
    @AllArgsConstructor
    public static class DimensionBoundaryFitResult {

        final boolean fits;
        final MetObject object;
    }
}
