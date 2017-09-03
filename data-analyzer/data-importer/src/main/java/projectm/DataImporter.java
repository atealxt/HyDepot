package projectm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projectm.persist.dao.LogRepository;
import projectm.persist.entity.Log;

@Service
public class DataImporter {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private LogRepository logRepository;
	private final static Pattern PATTERN_RAW_LOG = Pattern
			.compile("[^\\s]+ [^\\s]+  [^\\s]+ [^\\s]+ [^\\s]+ [^\\s]+ (.+)");
	private final static Pattern PATTERN_DOC_ID = Pattern
			.compile("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}");
	private final static String FILES = "/files/";
	private final static Pattern PATTERN_FILE_ID = Pattern.compile(FILES + "(\\d+)");

	public void importDataFromCsvToDB() throws IOException {

		File folder = new File("D:\\project-m-logs");
		File[] listOfFiles = folder.listFiles();

		long total = 0;
		long included = 0;

		for (File file : listOfFiles) {
			logger.info("Reading " + file);
			boolean header = true;
			Reader in = new FileReader(file);
			Iterable<CSVRecord> records = CSVFormat.RFC4180
					.withHeader("_raw", "_time", "host", "index", "linecount", "source", "sourcetype", "splunk_server")
					.parse(in);
			for (CSVRecord record : records) {
				if (header) {
					header = false;
					continue;
				}
				String raw = record.get("_raw");
				String time = record.get("_time");
				// String host = record.get("host");
				// String index = record.get("index");
				// String lineCount = record.get("linecount");
				// String source = record.get("source");
				// String sourceType = record.get("sourcetype");
				// String server = record.get("splunk_server");

				total++;

				Matcher matcherRaw = PATTERN_RAW_LOG.matcher(raw);
				if (!matcherRaw.find()) {
					continue;
				}

				Matcher matcherFileId = PATTERN_FILE_ID.matcher(raw);
				Matcher matcherDocId = PATTERN_DOC_ID.matcher(raw);
				boolean foundFileId = matcherFileId.find(), foundDocId = matcherDocId.find();
				if (!foundFileId && !foundDocId) {
					// logger.info("skip useless log: " + raw);
					continue;
				}

				String docId = matcherDocId.group(0);
				Long fileId = null;
				if (foundFileId) {
					fileId = Long.valueOf(matcherFileId.group(0).substring(FILES.length()));
				}

				Log log = new Log(docId, fileId, raw, time);
				logRepository.save(log);
				included++;
				if (log.getId() % 1000 == 0) {
					logger.info("Imported " + log.getId() + " records. " + //
							" Included rate: " + (included / (double) total));
				}
			}
		}
	}
}
