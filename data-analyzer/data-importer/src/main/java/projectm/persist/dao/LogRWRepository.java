package projectm.persist.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import projectm.persist.entity.LogRW;

public interface LogRWRepository extends CrudRepository<LogRW, Long>, QueryByExampleExecutor<LogRW> {
}
