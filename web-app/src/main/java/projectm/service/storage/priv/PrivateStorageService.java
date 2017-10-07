package projectm.service.storage.priv;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import projectm.service.storage.StorageException;
import projectm.service.storage.StorageService;

@Service("PrivateStorageService")
public class PrivateStorageService implements StorageService {

	@Override
	public void store(String bucketName, String documentId, byte[] content, String contentType)
			throws StorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] fetch(String bucketName, String documentId, HttpServletResponse response) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String bucketName, String documentId) throws StorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public void colder(String bucketName, String documentId) throws StorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public void hotter(String bucketName, String documentId) throws StorageException {
		// TODO Auto-generated method stub

	}

}
