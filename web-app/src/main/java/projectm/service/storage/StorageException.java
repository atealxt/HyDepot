package projectm.service.storage;

public class StorageException extends RuntimeException {

	private static final long serialVersionUID = -6911928917755827189L;

	public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
