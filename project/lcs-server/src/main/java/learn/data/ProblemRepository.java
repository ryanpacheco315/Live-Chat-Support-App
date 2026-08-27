package learn.data;

import learn.models.Problem;

public interface ProblemRepository {
    Problem findById(int id) throws DataAccessException;

    Problem create(Problem problem) throws DataAccessException;
}
