package projectm.service.storage;

public interface StorageService {

	void store(String bucketName, String documentId, byte[] content, String contentType) throws StorageException;

	byte[] fetch(String bucketName, String documentId) throws StorageException;

	void delete(String bucketName, String documentId) throws StorageException;

	void colder(String bucketName, String documentId) throws StorageException;

	void hotter(String bucketName, String documentId) throws StorageException;

	// TODO signed url
}
