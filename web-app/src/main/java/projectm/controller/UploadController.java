package projectm.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.atomix.concurrent.DistributedLock;
import projectm.ApplicationConfig;
import projectm.Constants;
import projectm.service.storage.StorageException;
import projectm.service.storage.StorageService;

@RestController
@RequestMapping("api")
public class UploadController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ApplicationConfig appConfig;

	private ExecutorService executorService;

	@PostConstruct
	public void init() {
		executorService = new ThreadPoolExecutor(0, 500, // TODO thread pool size configurable.
				120L, TimeUnit.SECONDS, //
				new LinkedBlockingQueue<Runnable>());
	}

	@PostMapping("/upload")
	public String upload(//
			@RequestParam(value = "platformCode", required = true) String platformCode,
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "documentId", required = false) String documentId,
			@RequestParam(value = "consensusLevel", required = false, defaultValue = "0") int consensusLevel,
			@RequestParam(value = "replicate", required = false, defaultValue = "true") boolean replicate)
			throws StorageException, IOException, InterruptedException, ExecutionException {

		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		if (file.isEmpty()) {
			throw new StorageException("Failed to store empty file " + filename);
		}
		if (filename.contains("..")) {
			// This is a security check
			throw new StorageException("Cannot store file with relative path outside current directory " + filename);
		}

		// get lock
		DistributedLock lock = appConfig.getLockService().getLock(platformCode + "-" + documentId).join();
		try {
			lock.lock().join();

			if (!replicate) {
				Future<?> future = executorService
						.submit(createUploadTask(appConfig.primaryStorage(), platformCode, documentId, file));
				if (consensusLevel != Constants.CONSENSUS_LEVEL_LOW) {
					future.get();
				}
				return "Upload success!";
			} else {
				List<StorageService> storageServices = appConfig.storages();
				List<Future<?>> tasks = new ArrayList<Future<?>>();
				for (StorageService storageService : storageServices) {
					Future<?> future = executorService
							.submit(createUploadTask(storageService, platformCode, documentId, file));
					tasks.add(future);
				}
				if (consensusLevel == Constants.CONSENSUS_LEVEL_HIGH) {
					for (Future<?> future : tasks) {
						future.get();
					}
				} else if (consensusLevel == Constants.CONSENSUS_LEVEL_MIDDLE) {
					tasks.get(0).get();
				}
				return "Upload success!";
			}
		} finally {
			lock.unlock().join();
			lock.close().join();
		}
	}

	private Runnable createUploadTask(StorageService storageService, String platformCode, String documentId,
			MultipartFile file) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					storageService.store(platformCode, documentId, file.getBytes(), file.getContentType());
				} catch (StorageException | IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		};
	}
}
