package server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.Gson;

import gui.Monitor;
import message.Message;
import message.MessageData;
import message.Message.MessageType;
import message.MessageData.ResponseStatus;

public class SocketThread extends Thread {
	private Socket socket;
	private Monitor monitor;
	BufferedReader request;
	BufferedWriter response;

	public SocketThread(Socket socket, Monitor monitor) {
		this.socket = socket;
		this.monitor = monitor;
	}

	@Override
	public void run() {
		Gson gson = new Gson();
		this.monitor.setTitle(getIPAddress());
		this.monitor.setDirectory("");
		this.monitor.setConn(this);
		try {
			request = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			response = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			String requestStr;
			Message responseMsg;
			MessageData responseMsgData;
			while (!Thread.currentThread().isInterrupted()) {
				requestStr = request.readLine();
				if (requestStr == null) {
					System.out.println("Connection closed");
					break;
				}

				responseMsg = gson.fromJson(requestStr, Message.class);
				switch (responseMsg.getType()) {
					case REQUEST:
						System.out.println("Invalid message type");
						break;
					case RESPONSE:
						responseMsgData = responseMsg.getData();
						switch (responseMsgData.getCommand()) {
							case GET_DRIVES:
								this.monitor.directoryChooser
										.createDrives(Arrays.asList(responseMsgData.getData().split("\\|")));
								break;
							case GET_DIRS:
								this.monitor.directoryChooser
										.addChildren(responseMsgData.getData().split("\\|"));
								break;
							case WATCH:
								responseMsgData = responseMsg.getData();
								if (responseMsgData.getStatus() == ResponseStatus.OK) {
									this.monitor.startWatching();
								} else {
									this.monitor.showErrorMessage(responseMsgData.getData());
								}
								break;
							case STOP:
								this.monitor.stopWatching();
								break;
							case DISCONNECT:
								this.monitor.dispose();
								this.interrupt();
								break;
							default:
								System.out.println("Invalid command");
								break;
						}
						break;
					case EVENT:
						responseMsgData = responseMsg.getData();
						this.monitor.addEvent(responseMsgData.getEvent(), responseMsgData.getData(),
								responseMsg.getTimestamp());
						break;
					default:
						System.out.println("Invalid message type");
						break;
				}
			}
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws Exception {
		response.close();
		request.close();
		socket.close();
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public String getIPAddress() {
		return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
	}

	public void sendRequest(MessageData requestMsgData) throws IOException {
		Gson gson = new Gson();
		Message requestMsg = new Message(MessageType.REQUEST, requestMsgData);
		response.write(gson.toJson(requestMsg));
		response.newLine();
		response.flush();
	}

	public void disconnect() throws Exception {
		sendRequest(new MessageData(MessageData.CommandType.DISCONNECT, null));
	}
}