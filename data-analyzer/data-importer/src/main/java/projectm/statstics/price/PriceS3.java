package projectm.statstics.price;

import static java.lang.Math.max;

public class PriceS3 {

	private static final int MAX_CALC_DAYS = 365;
	private static final double STORAGE_PRICE_UNIT_CLASS1 = 0.023d; // Standard Storage
	private static final double STORAGE_PRICE_UNIT_CLASS2 = 0.0125d; // Infrequent Access Storage †
	@SuppressWarnings("unused")
	private static final double OPERATION_PRICE_WRITE_UNIT_CLASS1 = 0.005d; // Standard Storage
	private static final double OPERATION_PRICE_READ_UNIT_CLASS1 = 0.004d; // Standard Storage
	private static final double OPERATION_PRICE_UNIT_CLASS2 = 0.01d; // Infrequent Access Storage †
	private static final double PRICE_CHANGE_CLASS = 0.01d / 1000d; // price of change class

	public void calc(double sizeInMB, int existDaysInClass1) {

		for (int existDays = existDaysInClass1 + 1; existDays <= MAX_CALC_DAYS; existDays++) {

			// storage price
			double storagePrice1 = STORAGE_PRICE_UNIT_CLASS1 * (sizeInMB / 1024d) * (existDays / 30.0);
			int existDaysInClass2 = existDays - existDaysInClass1;
			double storagePrice2 = STORAGE_PRICE_UNIT_CLASS1 * (sizeInMB / 1024d) * (existDaysInClass1 / 30.0) + //
					STORAGE_PRICE_UNIT_CLASS2 * max(sizeInMB / 1024, 128 / 1024d / 1024d)
							* (max(existDaysInClass2, 30) / 30.0);

			int thresholdNumOfReadWrite = -1;
			double thresholdPrice1 = -1;
			double thresholdPrice2 = -1;

			for (int numOfReadWrite = 0; numOfReadWrite < Integer.MAX_VALUE; numOfReadWrite++) {

				// use read price to calc operation price in class1 since write is a little expensive than read.
				// class2 cheaper than cheaper class1 is ensured that move is valuable.
				double operationPrice1 = OPERATION_PRICE_READ_UNIT_CLASS1 * numOfReadWrite;
				double operationPrice2 = OPERATION_PRICE_UNIT_CLASS2 * numOfReadWrite;

				double price1 = storagePrice1 + operationPrice1;
				double price2 = storagePrice2 + operationPrice2 + PRICE_CHANGE_CLASS;

				if (price1 < price2) {
					if (thresholdNumOfReadWrite != -1) {
						String out = "A file size " + sizeInMB + "MB stayed in class1 in " + existDaysInClass1 + " days, ";
						out += "will cost less if moved to class2 and stay " + existDaysInClass2 + " days ";
						out += "with at most read/write " + thresholdNumOfReadWrite + " times. ";
						out += "Cost1: " + thresholdPrice1 + ", Cost2: " + thresholdPrice2;
						System.out.println(out);
					}
					break;
				} else {
					thresholdNumOfReadWrite = numOfReadWrite;
					thresholdPrice1 = price1;
					thresholdPrice2 = price2;
				}
			}
		}
	}

}
