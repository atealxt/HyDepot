package projectm.service.storage.oss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;

import projectm.ApplicationConfig;
import projectm.service.storage.StorageException;
import projectm.service.storage.StorageService;

@Service("OSSStorageService")
public class OSSStorageService implements StorageService {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private OSSClient ossClient;

	@Autowired
	private ApplicationConfig applicationConfig;

	@PostConstruct
	public void init() {
		String endpoint = "http://oss-cn-beijing.aliyuncs.com";
		String accessKeyId = applicationConfig.getOssAccessKeyId();
		String accessKeySecret = applicationConfig.getOssAccessKeySecret();
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
	public byte[] fetch(String bucketName, String documentId, HttpServletResponse response) throws StorageException {
		OSSObject obj = ossClient.getObject(bucketName, documentId);
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + documentId);
		response.setContentType(obj.getObjectMetadata().getContentType());
		InputStream stream = obj.getObjectContent();
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
