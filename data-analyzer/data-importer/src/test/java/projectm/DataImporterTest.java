package projectm;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@Transactional
@SpringBootTest
public class DataImporterTest {

	@Autowired
	private DataImporter importer;

	@Test
	public void testImportDataFromCsvToDB() throws IOException {
		importer.importDataFromCsvToDB();
	}
}
