package de.eblaas.museum;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetObject {

    @CsvBindByName(column = "Object ID", required = true)
    private long id;

    @CsvBindByName(column = "Dimensions")
    private String dimensionRaw;

    private Dimension dimension;

    boolean validDimension() {
        if (dimension == null) {
            dimension = Dimension.fromString(dimensionRaw);
        }
        return dimension != null;
    }
}
