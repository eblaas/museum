package de.eblaas.museum;

import de.eblaas.museum.Dimension.DimensionBoundary;
import de.eblaas.museum.Dimension.DimensionBoundaryFitResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MetObjectService {

    static int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    void initDb() {
        String[] sqlStatements = {
                "drop table met_objects if exists",
                // primary index on id to find objects by id efficiently
                "create table met_objects(id BIGINT PRIMARY KEY, dim VARCHAR(5000), height DOUBLE, width DOUBLE, depth DOUBLE, weight DOUBLE)",
                // secondary index to find objects by dimensions efficiently, order defined by importance of dimension
                "create index dimensions on met_objects(height, width, depth, weight)"
        };
        Arrays.asList(sqlStatements).forEach(jdbcTemplate::execute);
    }

    void batchInsert(List<MetObject> objects) {

        log.info("Insert data batch ... size={}", objects.size());

        jdbcTemplate.batchUpdate(
                "insert into met_objects (id, dim, height, width, depth, weight) values(?,?,?,?,?,?)",
                objects,
                BATCH_SIZE,
                (ps, obj) -> {
                    ps.setLong(1, obj.getId());
                    ps.setString(2, obj.getDimensionRaw());
                    ps.setDouble(3, obj.getDimension().getHeight());
                    ps.setDouble(4, obj.getDimension().getWidth());
                    ps.setDouble(5, obj.getDimension().getDepth());
                    ps.setDouble(6, obj.getDimension().getWeight());
                });

    }

    private MetObject mapToMetObject(ResultSet rs) throws SQLException {
        return MetObject.builder()
                .id(rs.getLong("id"))
                .dimensionRaw(rs.getString("dim"))
                .dimension(new Dimension(
                        rs.getDouble("height"),
                        rs.getDouble("width"),
                        rs.getDouble("depth"),
                        rs.getDouble("weight")
                )).build();
    }

    private MetObject findById(long id) {

        var sql = "select * from met_objects WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> mapToMetObject(rs));
    }


    /**
     * Check if a met object is within specified dimension boundaries.
     *
     * @param id                the met object id
     * @param dimensionBoundary the dimension boundaries to check for
     * @return returns {@link DimensionBoundaryFitResult#fits} true if the object is within the specified dimension
     * boundaries, else false. If a object is not found by id (dimensions not available/parsable) return false.
     */
    DimensionBoundaryFitResult doesItFit(Long id, DimensionBoundary dimensionBoundary) {

        var object = findById(id);
        var fits = false;

        if (object != null) {
            fits = object.getDimension().doesItFit(dimensionBoundary);
        }

        return new DimensionBoundaryFitResult(fits, object);
    }

    /**
     * List fitting MET objects for the specified dimension boundaries.
     * <p>IMPORTANT: NOT every object has ALL dimensions</p>
     * <p> min boundaries, require the dimension to be available, else considered as not fitting. <p>
     * <p> max boundaries, don't require the dimension to be available, only limiting it if present. </p>
     *
     * @param dimensionBoundary the dimension boundaries to check for
     * @return MET objects fitting the specified dimension boundaries
     */
    List<DimensionBoundaryFitResult> listFittingObjects(DimensionBoundary dimensionBoundary) {

        var sql = "select * from met_objects "
                + "where  height between ? and ? "
                + "and width between ? and ? "
                + "and depth between ? and ? "
                + "and weight between ? and ? "
                + "limit 50";

        return jdbcTemplate.query(sql, dimensionBoundary.args(),
                (rs, n) -> new DimensionBoundaryFitResult(true, mapToMetObject(rs)));
    }
}
