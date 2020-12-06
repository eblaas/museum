package de.eblaas.museum;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DimensionTest {

    @Test
    void testDimensionWithSingleDimension() {

        var dimension = Dimension.fromString("Diam. 11/16 in. (1.7 cm)");

        assertThat(dimension).isNotNull();
        assertThat(dimension.getHeight()).isEqualTo(dimension.getWidth());
        assertThat(dimension.getHeight()).isEqualTo(1.7D);
        assertThat(dimension.getDepth()).isEqualTo(-1);
        assertThat(dimension.getWeight()).isEqualTo(-1);
    }

    @Test
    void testDimensionWithTwoDimension() {

        var dimension = Dimension.fromString("23 1/4 x 18 1/4 in. (59.1 x 46.4 cm)");

        assertThat(dimension).isNotNull();
        assertThat(dimension.getHeight()).isEqualTo(59.1D);
        assertThat(dimension.getWidth()).isEqualTo(46.4D);
        assertThat(dimension.getDepth()).isEqualTo(-1);
        assertThat(dimension.getWeight()).isEqualTo(-1);
    }

    @Test
    void testDimensionWithThreeDimension() {

        var dimension = Dimension.fromString("46 1/4 x 24 7/8 x 17 5/8 in. (117.5 x 63.2 x 44.8 cm)");

        assertThat(dimension).isNotNull();
        assertThat(dimension.getHeight()).isEqualTo(117.5D);
        assertThat(dimension.getWidth()).isEqualTo(63.2D);
        assertThat(dimension.getDepth()).isEqualTo(44.8D);
        assertThat(dimension.getWeight()).isEqualTo(-1);
    }

    @Test
    void testDimensionWithForeDimension() {

        var dimension = Dimension.fromString("11 3/16 x 14 7/16 x 11 3/8 in. (28.4 x 36.7 x 28.9 cm); 41 oz. 5 dwt. (1282.7 g)");

        assertThat(dimension).isNotNull();
        assertThat(dimension.getHeight()).isEqualTo(28.4D);
        assertThat(dimension.getWidth()).isEqualTo(36.7D);
        assertThat(dimension.getDepth()).isEqualTo(28.9D);
        assertThat(dimension.getWeight()).isEqualTo(1282.7D);
    }

    @Test
    void testDimensionWithForeDimensionAndUnits() {

        var dimension = Dimension.fromString("11 3/16 x 14 7/16 x 11 3/8 in. (28.4 x 36.7 x 28.9 mm, 5.1 kg)");

        assertThat(dimension).isNotNull();
        assertThat(dimension.getHeight()).isEqualTo(2.84D);
        assertThat(dimension.getWidth()).isEqualTo(3.67D);
        assertThat(dimension.getDepth()).isEqualTo(2.89D);
        assertThat(dimension.getWeight()).isEqualTo(5100D);
    }
}
