package projectm.persist.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import projectm.persist.entity.Log;

public interface LogRepository extends CrudRepository<Log, Long>, QueryByExampleExecutor<Log> {

	@Query(nativeQuery = true, value = "SELECT tt.doc_id " + //
			"FROM  " + //
			"( " + //
			"SELECT t.doc_id, t.log_date, COUNT(*) AS cnt " + //
			"FROM LOG t " + //
			"GROUP BY t.doc_id, t.log_date  " + //
			")tt " + //
			"GROUP BY tt.doc_id " + //
			"ORDER BY COUNT(tt.doc_id) DESC limit ?#{[0]} ") //
	List<String> getTopRWLogs(int limit);

	@Query(nativeQuery = true, value = "SELECT t.log_date as 'Date', COUNT(*) as 'R/W Count' " +
			"FROM LOG t " +
			"WHERE t.doc_id = ?#{[0]} " +
			"GROUP BY t.log_date " +
			"ORDER BY t.log_date ASC") //
	List<Object[]> getLogRW(String docId);

}
