package projectm.service.storage.oss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;

import projectm.service.storage.StorageException;
import projectm.service.storage.StorageService;

@Service("OSSStorageService")
public class OSSStorageService implements StorageService {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private OSSClient ossClient;

	@PostConstruct
	public void init() {
		// TODO config
		String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
		String accessKeyId = "accessKeyId";
		String accessKeySecret = "accessKeySecret";
		ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
	}

	@Override
	public void store(String bucketName, String documentId, byte[] content, String contentType)
			throws StorageException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(content.length);
		metadata.setContentType(contentType);
		try (InputStream contentStream = new ByteArrayInputStream(content)) {
			ossClient.putObject(bucketName, documentId, contentStream, metadata);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public byte[] fetch(String bucketName, String documentId) throws StorageException {
		InputStream stream = ossClient.getObject(bucketName, documentId).getObjectContent();
		try {
			return IOUtils.toByteArray(stream);
		} catch (IOException e) {
			throw new StorageException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String bucketName, String documentId) throws StorageException {
		ossClient.deleteObject(bucketName, documentId);
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
