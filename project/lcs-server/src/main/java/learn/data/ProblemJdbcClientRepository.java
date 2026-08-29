package learn.data;

import learn.data.mappers.ProblemMapper;
import learn.models.Problem;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProblemJdbcClientRepository implements ProblemRepository {
    private final JdbcClient jdbcClient;

    public ProblemJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static final String BASE_SELECT = "select id, category, subcategory, description from problem";

    @Override
    public Problem findById(int id) throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where id = ?")
                .param(id)
                .query(new ProblemMapper())
                .optional().orElse(null);
    }

    @Override
    public Problem create(Problem problem) throws DataAccessException {
        final String sql = """
                insert into problem (category, subcategory, description)
                values (:category, :subcategory, :description);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcClient.sql(sql)
                .param("category", problem.getCategory().name())
                .param("subcategory", problem.getSubcategory())
                .param("description", problem.getDescription())
                .update(keyHolder, "id");

        if (rowsAffected == 0) {
            return null;
        }

        problem.setId(keyHolder.getKey().intValue());
        return problem;
    }
}
