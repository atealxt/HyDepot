package projectm.statstics.price;

import org.junit.Test;

public class PriceTest {

	private PriceS3 s3Calculator = new PriceS3();
	private PriceGCS gcsCalculator = new PriceGCS();
	private PriceOSS ossCalculator = new PriceOSS();

	@Test
	public void testCalcS3() {

		double SIZE_10K = 10 / 1024.0;
		double SIZE_100K = 100 / 1024.0;
		double SIZE_500K = 500 / 1024.0;
		double SIZE_1M = 1;
		double SIZE_10M = 10;
		double SIZE_100M = 100;
		double SIZE_1G = 1024;

		double sizes[] = new double[] { SIZE_10K, SIZE_100K, SIZE_500K, SIZE_1M, SIZE_10M, SIZE_100M,
				SIZE_1G };

		for (double size : sizes) {
			s3Calculator.calc(size, 30);
		}
	}

	@Test
	public void testCalcGCS() {

		double SIZE_10K = 10 / 1024.0;
		double SIZE_100K = 100 / 1024.0;
		double SIZE_500K = 500 / 1024.0;
		double SIZE_1M = 1;
		double SIZE_10M = 10;
		double SIZE_100M = 100;
		double SIZE_1G = 1024;

		double sizes[] = new double[] { SIZE_10K, SIZE_100K, SIZE_500K, SIZE_1M, SIZE_10M, SIZE_100M,
				SIZE_1G };

		for (double size : sizes) {
			gcsCalculator.calc(size, 30);
		}
	}

	@Test
	public void testCalcOSS() {

		double SIZE_10K = 10 / 1024.0;
		double SIZE_100K = 100 / 1024.0;
		double SIZE_500K = 500 / 1024.0;
		double SIZE_1M = 1;
		double SIZE_10M = 10;
		double SIZE_100M = 100;
		double SIZE_1G = 1024;

		double sizes[] = new double[] { SIZE_10K, SIZE_100K, SIZE_500K, SIZE_1M, SIZE_10M, SIZE_100M,
				SIZE_1G };

		for (double size : sizes) {
			ossCalculator.calc(size, 30);
		}
	}

}
