package projectm.persist.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import projectm.persist.entity.RawLog;

public interface RawLogRepository extends CrudRepository<RawLog, Long>, QueryByExampleExecutor<RawLog> {
}
