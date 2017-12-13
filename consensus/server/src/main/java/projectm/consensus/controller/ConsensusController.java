package projectm.consensus.controller;

import java.io.IOException;

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
import projectm.consensus.NodeAddress;
import projectm.consensus.State;
import projectm.consensus.service.DefaultConsensusServer;
import projectm.consensus.service.NotifyResult;
import projectm.consensus.service.Resource;

@RestController
@RequestMapping("api/consensus")
public class ConsensusController {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DefaultConsensusServer consensusServer;

	@PostMapping("/state")
	public State changeState(//
			@ApiParam(value = "State Value") //
			@RequestParam(value = "state", required = true) String state, //
			HttpServletRequest request, HttpServletResponse response) {
		return consensusServer.transition(State.parse(state));
	}

	@GetMapping("/state")
	public State getState(//
			HttpServletRequest request, HttpServletResponse response) {
		return consensusServer.getState();
	}

	@GetMapping("/notify")
	public NotifyResult notify(//
			@ApiParam(value = "IP") //
			@RequestParam(value = "ip", required = true) String ip, //
			@ApiParam(value = "Port") //
			@RequestParam(value = "port", required = true) String port, //
			@ApiParam(value = "State Value") //
			@RequestParam(value = "state", required = true) String state, //
			HttpServletRequest request, HttpServletResponse response) {
		return consensusServer.notify(new NodeAddress(ip, Integer.parseInt(port)), State.parse(state));
	}

	@GetMapping("/resource")
	public Resource getResource(//
			@ApiParam(value = "key") //
			@RequestParam(value = "key", required = true) String key, //
			HttpServletRequest request, HttpServletResponse response) {
		// redirect to leader if it's not.
		if (consensusServer.getState() != State.LEADER) {
			NodeAddress leader = consensusServer.getLeaderAddress();
			response.setStatus(HttpServletResponse.SC_FOUND);
			response.setHeader("Location", leader.getHttpAddr() + "/api/consensus/resource?key=" + key);
			return null;
		}
		Resource resource = consensusServer.getResource(key);
		if (resource == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return resource;
	}

	@PostMapping("/resource")
	public Resource updateResource(//
			@ApiParam(value = "key") //
			@RequestParam(value = "key", required = true) String key, //
			@ApiParam(value = "value") //
			@RequestParam(value = "value", required = false) String value, //
			@ApiParam(value = "id") //
			@RequestParam(value = "id", required = false) Long id, //
			@ApiParam(value = "delete") //
			@RequestParam(value = "delete", required = false) boolean delete, //
			@ApiParam(value = "leader") //
			@RequestParam(value = "leader", required = false, defaultValue = "true") boolean leader, //
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (leader && consensusServer.getState() != State.LEADER) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String msg = "This is not leader, you need to resend the request to " + consensusServer.getLeaderAddress();
			logger.warn(msg);
			response.getOutputStream().write(msg.getBytes());
			return null;
		}
		if (delete) {
			return consensusServer.deleteResource(key, id);
		} else {
			return consensusServer.addResource(key, value, id);
		}
	}
}
