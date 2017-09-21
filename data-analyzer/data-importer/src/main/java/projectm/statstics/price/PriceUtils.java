package projectm.statstics.price;

public final class PriceUtils {

	private PriceUtils () {}

	public static String format(double d) {
		return String.format("%1$,.4f", d);
	}

}
