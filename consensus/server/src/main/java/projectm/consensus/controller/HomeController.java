package projectm.consensus.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import projectm.consensus.ApplicationConfig;
import projectm.consensus.service.DefaultConsensusServer;
import projectm.consensus.service.DefaultConsensusServerL2;

@RestController
@RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/html")
public class HomeController {

	@Resource
	private ApplicationConfig appConfig;
	@Resource
	private DefaultConsensusServer consensusServer;
	@Resource
	private DefaultConsensusServerL2 consensusServerL2;

	@RequestMapping
	public String home() {
		String str = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
				+ //
				"<html>\r\n" + //
				"<head>\r\n" + //
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n" + //
				"<title>Project M Consensus Server</title>\r\n" + //
				"</head>\r\n" + //
				"<body>" + //

				"	<h2>Project M Consensus Server</h2>\r\n" + //

				"	<table>\r\n" + //
				"		<tr>\r\n" + //
				"			<td>IP</td>\r\n" + //
				"			<td>" + appConfig.getIp()//
				+ "</td>\r\n" + //
				"		</tr>\r\n" + //
				"		<tr>\r\n" + //
				"			<td>Local IP</td>\r\n" + //
				"			<td>" + appConfig.getIpLocal()//
				+ "</td>\r\n" + //
				"		</tr>\r\n" + //
				"		<tr>\r\n" + //
				"			<td>Port</td>\r\n" + //
				"			<td>" + appConfig.getPort()//
				+ "</td>\r\n" + //
				"		</tr>\r\n" + //
				"		<tr>\r\n" + //
				"			<td>Cluster</td>\r\n" + //
				"			<td>" + appConfig.getNodes()//
				+ "</td>\r\n" + //
				"		</tr>\r\n" + //
				"		<tr>\r\n" + //
				"			<td>Status</td>\r\n" + //
				"			<td>" + (appConfig.cluster().isEmpty() ? "" : consensusServer.getState())//
				+ "</td>\r\n" + //
				"		</tr>\r\n" + //
				"		<tr>\r\n" + //
				"			<td>Second Level Cluster</td>\r\n" + //
				"			<td>" + appConfig.getNodesL2() + "</td>\r\n" + //
				"		</tr>\r\n" + //
				"		<tr>\r\n" + //
				"			<td>Status</td>\r\n" + //
				"			<td>" + (appConfig.clusterL2().isEmpty() ? "" : consensusServerL2.getState())//
				+ "</td>\r\n" + //
				"		</tr>\r\n" + //
				"	</table>"//
				+ "<br><a href='swagger-ui.html'>swagger-ui.html</a>" + "</body>\r\n" + //
				"</html>";
		return str;
	}
}
