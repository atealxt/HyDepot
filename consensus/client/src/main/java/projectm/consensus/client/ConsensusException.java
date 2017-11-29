package projectm.consensus.client;

public class ConsensusException extends RuntimeException {

	private static final long serialVersionUID = -4282457396010218663L;

	public ConsensusException() {
		super();
	}

	public ConsensusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConsensusException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConsensusException(String message) {
		super(message);
	}

	public ConsensusException(Throwable cause) {
		super(cause);
	}
}
