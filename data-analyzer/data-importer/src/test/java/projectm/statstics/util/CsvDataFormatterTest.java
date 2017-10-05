package projectm.statstics.util;

import java.io.IOException;

import org.junit.Test;

public class CsvDataFormatterTest {

	@Test
	public void testFormat() throws IOException {
		new CsvDataFormatter().format("D:\\Git_p\\project-m\\data-analyzer\\storage-predict\\data1.csv");
	}

}
