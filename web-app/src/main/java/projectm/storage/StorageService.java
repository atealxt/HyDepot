package projectm.storage;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty()) {
        	throw new StorageException("Failed to store empty file " + filename);
        }
        if (filename.contains("..")) {
        	// This is a security check
        	throw new StorageException(
        			"Cannot store file with relative path outside current directory "
        					+ filename);
        }
        
        // TODO
        try {
        	logger.debug(file.getOriginalFilename());
        	logger.debug(file.getContentType());
			logger.debug(file.getBytes().length + "");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
