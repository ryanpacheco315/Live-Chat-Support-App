package learn.data;

import learn.models.TimeRecord;

public interface TimeRecordRepository {
    TimeRecord findById(int id) throws DataAccessException;

    TimeRecord create(TimeRecord timeRecord) throws DataAccessException;

    boolean update(TimeRecord timeRecord) throws DataAccessException;
}
