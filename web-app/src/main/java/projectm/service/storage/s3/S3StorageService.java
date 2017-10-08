package projectm.service.storage.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import projectm.ApplicationConfig;
import projectm.service.storage.StorageException;
import projectm.service.storage.StorageService;

@Service("S3StorageService")
public class S3StorageService implements StorageService {

	private AmazonS3 s3client;
	@Autowired
	private ApplicationConfig applicationConfig;
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void init() {
		AWSCredentials credentials = new BasicAWSCredentials(applicationConfig.getS3AccessKeyId(),
				applicationConfig.getS3AccessKeySecret());
		s3client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}

	@Override
	public void store(String bucketName, String documentId, byte[] content, String contentType)
			throws StorageException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(content.length);
		metadata.setContentType(contentType);
		try (InputStream contentStream = new ByteArrayInputStream(content)) {
			s3client.putObject(bucketName, documentId, contentStream, metadata);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public byte[] fetch(String bucketName, String documentId, HttpServletResponse response) throws StorageException {
		S3Object obj = s3client.getObject(bucketName, documentId);
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
	public void delete(String bucketName, String documentId) {
		s3client.deleteObject(bucketName, documentId);
	}

	@Override
	public void colder(String bucketName, String documentId) throws StorageException {
		// TODO Auto-generated method stub

		// way1
		// s3client.changeObjectStorageClass(bucketName, key, newStorageClass);

		// way2
		// CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName,
		// documentId, bucketName + "coder",
		// documentId).withStorageClass(StorageClass.StandardInfrequentAccess);
		// s3client.copyObject(copyObjectRequest);
	}

	@Override
	public void hotter(String bucketName, String documentId) throws StorageException {
		// TODO Auto-generated method stub
	}
}
