package client;

import gui.Monitor;
import server.SocketThread;

public class Client {
	private String host;
	private int port;
	private String status;
	private String directory;
	private String lastUpdate;
	private SocketThread conn;

	public Client(String host, int port, SocketThread conn) {
		this.host = host;
		this.port = port;
		this.status = "Connected";
		this.conn = conn;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getStatus() {
		return status;
	}

	public String getDirectory() {
		return directory;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public SocketThread getConn() {
		return conn;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@Override
	public String toString() {
		return "ClientInfo [host=" + host + ", port=" + port + ", status=" + status + ", directory=" + directory
				+ ", lastUpdate=" + lastUpdate + "]";
	}
}
