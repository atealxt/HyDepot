package projectm.persist.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import projectm.persist.entity.Log;

public interface LogRepository extends CrudRepository<Log, Long>, QueryByExampleExecutor<Log> {
}
