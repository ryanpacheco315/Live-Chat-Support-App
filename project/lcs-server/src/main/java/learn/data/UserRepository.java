package learn.data;

import learn.models.User;

public interface UserRepository {
    User findById(int id) throws DataAccessException;

    User findByUsername(String username) throws DataAccessException;

    User create(User user) throws DataAccessException;
}
