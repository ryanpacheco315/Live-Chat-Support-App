package learn.data;

import learn.data.mappers.UserMapper;
import learn.models.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserJdbcClientRepository implements UserRepository {
    private final JdbcClient jdbcClient;

    public UserJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static final String BASE_SELECT = """
            select id, full_name, username, password, role
            from user
            """;

    @Override
    public User findById(int id) throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where id = ?")
                .param(id)
                .query(new UserMapper())
                .optional().orElse(null);
    }

    @Override
    public User findByUsername(String username) throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where username = ?")
                .param(username)
                .query(new UserMapper())
                .optional().orElse(null);
    }

    @Override
    public User create(User user) throws DataAccessException {
        final String sql = """
                insert into user (full_name, username, password, role)
                values (:full_name, :username, :password, :role);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcClient.sql(sql)
                .param("full_name", user.getFullName())
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .param("role", user.getRole().name())
                .update(keyHolder, "id");

        if (rowsAffected == 0) {
            return null;
        }

        user.setId(keyHolder.getKey().intValue());
        return user;
    }
}
