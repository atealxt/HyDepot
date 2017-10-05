package projectm.statstics.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public final class CsvDataFormatter {

	public void format(String filePath) throws IOException {
		String file = new String(Files.readAllBytes(Paths.get(filePath)));
		List<String> list = Arrays.asList(file.split("\r\n"));
		try {
			String pre = null;
			for (int i = 0; i < list.size(); i++) {
				String str = list.get(i);
				if (i == 0 || str.trim().isEmpty()) {
					System.out.println(str);
					continue;
				}
				String date = str.split(",")[0];
				date = date.substring(1, date.length() - 1);
				printZeroDays(pre, date);
				pre = date;
				System.out.println(str);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void printZeroDays(String pre, String date) throws ParseException {
		if (pre == null || date.trim().isEmpty()) {
			return;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(formatter.parse(pre));
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(formatter.parse(date));
		cal1.add(Calendar.DATE, 1);
		while (cal1.before(cal2)) {
			System.out.println("\"" + formatter.format(cal1.getTime()) + "\",\"0\"");
			cal1.add(Calendar.DATE, 1);
		}
	}
}
