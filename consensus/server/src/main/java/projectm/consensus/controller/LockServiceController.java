package projectm.consensus.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;
import projectm.consensus.ApplicationConfig;

@RestController
@RequestMapping("api/lock-service")
public class LockServiceController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ApplicationConfig appConfig;

	@PostMapping("/lock")
	public void lock(//
			@ApiParam(value = "Lock Key") //
			@RequestParam(value = "key", required = true) String key, //
			HttpServletRequest request, HttpServletResponse response) {
	}

	@PostMapping("/unlock")
	public void unlock(//
			@ApiParam(value = "Lock Key") //
			@RequestParam(value = "key", required = true) String key, //
			HttpServletRequest request, HttpServletResponse response) {
	}
}
