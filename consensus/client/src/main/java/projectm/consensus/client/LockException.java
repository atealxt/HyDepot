package projectm.consensus.client;

public class LockException extends RuntimeException {

	private static final long serialVersionUID = -7022260394925017218L;

	public LockException() {
		super();
	}

	public LockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LockException(String message, Throwable cause) {
		super(message, cause);
	}

	public LockException(String message) {
		super(message);
	}

	public LockException(Throwable cause) {
		super(cause);
	}
}
