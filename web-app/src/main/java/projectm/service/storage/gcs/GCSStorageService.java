package projectm.service.storage.gcs;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.netty.handler.ssl.PemPrivateKey;
import projectm.service.storage.StorageException;
import projectm.service.storage.StorageService;

@Service("GCSStorageService")
public class GCSStorageService implements StorageService {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private String serviceAccountEmail = "mocked";
	private PrivateKey privateKey = PemPrivateKey.valueOf("mocked".getBytes()) ;
	private String gcsProjectId;
	private Storage storage;
	private static final String USER_INFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
	private static final String CLOUD_STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";
	private static final List<String> serviceAccountScopes = Arrays.asList(USER_INFO_EMAIL_SCOPE, CLOUD_STORAGE_SCOPE);

	@PostConstruct
	public void init() {
		HttpTransport transport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(transport).setJsonFactory(jsonFactory)
				.setServiceAccountId(serviceAccountEmail).setServiceAccountScopes(serviceAccountScopes)
				.setServiceAccountPrivateKey(privateKey).build();
		String accessToken = credential.getAccessToken(); // FIXME timeout
		storage = StorageOptions.newBuilder().setCredentials(new GoogleCredentials(new AccessToken(accessToken, null)))
				.setProjectId(gcsProjectId).build().getService();
	}

	@Override
	public void store(String bucketName, String documentId, byte[] content, String contentType)
			throws StorageException {
		BlobId blobId = BlobId.of(bucketName, documentId);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
		storage.create(blobInfo, new ByteArrayInputStream(content));
	}

	@Override
	public byte[] fetch(String bucketName, String documentId) throws StorageException {
		BlobId blobId = BlobId.of(bucketName, documentId);
		return storage.readAllBytes(blobId);
	}

	@Override
	public void delete(String bucketName, String documentId) throws StorageException {
		BlobId blobId = BlobId.of(bucketName, documentId);
		storage.delete(blobId);
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
