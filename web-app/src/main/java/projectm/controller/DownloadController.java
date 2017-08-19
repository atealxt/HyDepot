package projectm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import projectm.ApplicationConfig;

@RestController
@RequestMapping("api")
public class DownloadController {

	@Autowired
	private ApplicationConfig appConfig;

	@GetMapping("/download")
	public void download(//
			@RequestParam(value = "platformCode", required = true, defaultValue = "D001") String platformCode,
			@RequestParam(value = "documentId", required = true, defaultValue = "doc-001") String documentId) {
		appConfig.primaryStorage().fetch(platformCode, documentId);
	}
}
