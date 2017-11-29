package projectm.consensus.client;

public class NodeAddress {

	private String ip;
	private int port;

	public NodeAddress(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "Node [ip=" + ip + ", port=" + port + "]";
	}
}
