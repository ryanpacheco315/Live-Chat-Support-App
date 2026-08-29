package learn.data.mappers;

import learn.models.Problem;
import learn.models.ProblemCategory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProblemMapper implements RowMapper<Problem> {

    @Override
    public Problem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Problem(
                rs.getInt("id"),
                ProblemCategory.valueOf(rs.getString("category")),
                rs.getString("subcategory"),
                rs.getString("description")
        );
    }
}
