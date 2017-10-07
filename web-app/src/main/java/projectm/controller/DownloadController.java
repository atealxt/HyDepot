package projectm.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;
import projectm.ApplicationConfig;
import projectm.service.storage.StorageService;

@RestController
@RequestMapping("api")
public class DownloadController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ApplicationConfig appConfig;

	@GetMapping("/download")
	public void download(//
			@ApiParam(value = "Identity of Customer/App") //
			@RequestParam(value = "platformCode", required = true) String platformCode,
			@ApiParam(value = "Identity of Document") //
			@RequestParam(value = "documentId", required = true) String documentId, //
			HttpServletResponse response) {
		for (StorageService storageService : appConfig.storages()) {
			try {
				byte[] object = storageService.fetch(platformCode, documentId, response);
				IOUtils.write(object, response.getOutputStream());
				return;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
