package projectm.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import projectm.ApplicationConfig;
import projectm.service.storage.StorageException;

@RestController
@RequestMapping("api")
public class FileUploadController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ApplicationConfig appConfig;

	@PostMapping("/upload")
	public String upload(//
			@RequestParam(value = "platformCode", required = true, defaultValue = "D001") String platformCode,
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "documentId", required = false, defaultValue = "doc-001") String documentId)
			throws StorageException, IOException {

		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		if (file.isEmpty()) {
			throw new StorageException("Failed to store empty file " + filename);
		}
		if (filename.contains("..")) {
			// This is a security check
			throw new StorageException("Cannot store file with relative path outside current directory " + filename);
		}

		appConfig.primaryStorage().store(platformCode, documentId, file.getBytes(), file.getContentType());
		return "File upload success!";
	}
}
