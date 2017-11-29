package projectm.consensus.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;
import projectm.consensus.ApplicationConfig;

@RestController
@RequestMapping("api/consensus")
public class ConsensusController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ApplicationConfig appConfig;

	@PostMapping("/state")
	public void changeState(//
			@ApiParam(value = "State Value") //
			@RequestParam(value = "state", required = true) String state, //
			HttpServletRequest request, HttpServletResponse response) {
	}

	@GetMapping("/state")
	public void getState(//
			HttpServletRequest request, HttpServletResponse response) {
	}

}
