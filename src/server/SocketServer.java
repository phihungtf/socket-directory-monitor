package server;

import gui.Main;
import java.io.*;
import java.net.*;

public class SocketServer extends Thread {
	private Main main;
	private SocketThread conn;
	private Socket socket;
	private ServerSocket serverSocket;
	private String host;
	private int port;

	public SocketServer(Main main) {
		this.main = main;
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Server started");

			while (!Thread.currentThread().isInterrupted()) {
				System.out.println("Waiting for client...");
				socket = serverSocket.accept();
				host = socket.getInetAddress().getHostAddress();
				port = socket.getPort();
				System.out.println("Client connected: " + host + ":" + port);
				// new thread for a client
				conn = new SocketThread(socket, main.createMonitor());
				conn.start();
				main.addClient(host, port, conn);
			}
		} catch (SocketException e) {
			System.out.println("Server stopped");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer() throws IOException {
		serverSocket.close();
		this.interrupt();
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}
}
