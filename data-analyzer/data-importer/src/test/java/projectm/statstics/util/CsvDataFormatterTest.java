package projectm.statstics.util;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

public class CsvDataFormatterTest {

	@Test
	public void testFormat() throws IOException {
		new CsvDataFormatter().format("D:\\Git_p\\project-m\\data-analyzer\\storage-predict\\data1.csv");
	}

	@Test
	public void testNormalization() throws IOException, ParseException {
		String name = "o_1";
		new CsvDataFormatter().normalization("D:\\Git_p\\project-m\\data-analyzer\\storage-predict\\" + name + ".csv");
	}

}
