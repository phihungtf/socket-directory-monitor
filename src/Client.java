import file.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import watcher.*;

import com.google.gson.*;

import message.*;
import message.Message.MessageType;
import message.MessageData.CommandType;
import message.MessageData.ResponseStatus;

public class Client implements WatchDir.Printable {
	private BufferedReader request;
	private BufferedWriter response;
	private WatchDir watchDir;

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			new Client("localhost", 6969);
		} else if (args.length == 1) {
			new Client(args[0], 6969);
		} else if (args.length == 2) {
			new Client(args[0], Integer.parseInt(args[1]));
		} else {
			System.out.println("Invalid arguments");
		}
	}

	public Client(String host, int port) {
		GsonBuilder builder = new GsonBuilder();
		// builder.setPrettyPrinting();
		Gson gson = builder.create();
		Boolean isRunning = true;

		try {
			try (Socket socket = new Socket(host, port)) {
				System.out.println("Connected to server: " + socket.getInetAddress().getHostAddress() + ":"
						+ socket.getPort());

				request = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				response = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				String requestStr, responseStr;
				Message requestMsg, responseMsg;
				MessageData requestMsgData, responseMsgData;
				while (isRunning) {
					requestStr = request.readLine();

					if (requestStr == null) {
						System.out.println("Connection closed");
						break;
					}

					requestMsg = gson.fromJson(requestStr, Message.class);
					if (requestMsg.getType() != MessageType.REQUEST) {
						System.out.println("Invalid message type");
						break;
					}

					requestMsgData = requestMsg.getData();
					switch (requestMsgData.getCommand()) {
						case GET_DRIVES:
							responseMsgData = new MessageData(
									ResponseStatus.OK,
									CommandType.GET_DRIVES,
									DirectoryExplorer.arrayToString(DirectoryExplorer.getDrives()));
							responseMsg = new Message(
									MessageType.RESPONSE,
									responseMsgData);
							responseStr = gson.toJson(responseMsg);
							response.write(responseStr);
							break;
						case GET_DIRS:
							responseMsgData = new MessageData(
									ResponseStatus.OK,
									CommandType.GET_DIRS,
									DirectoryExplorer
											.arrayToString(DirectoryExplorer.getDirectories(requestMsgData.getData())));
							responseMsg = new Message(
									MessageType.RESPONSE,
									responseMsgData);
							responseStr = gson.toJson(responseMsg);
							response.write(responseStr);
							break;
						case WATCH:
							String path = requestMsgData.getData();
							// register directory and process its events
							boolean recursive = true;
							Path dir = Paths.get(path);
							try {
								watchDir = new WatchDir(dir, recursive, this);
								watchDir.start();
								System.out.println("Watching " + path + "...");
								responseMsgData = new MessageData(
										ResponseStatus.OK,
										CommandType.WATCH,
										"Watching " + path + "...");
								responseMsg = new Message(
										MessageType.RESPONSE,
										responseMsgData);
								responseStr = gson.toJson(responseMsg);
								response.write(responseStr);
							} catch (AccessDeniedException e) {
								System.out.println("Access denied");
								responseMsgData = new MessageData(
										ResponseStatus.ERROR,
										CommandType.WATCH,
										"Access denied");
								responseMsg = new Message(
										MessageType.RESPONSE,
										responseMsgData);
								responseStr = gson.toJson(responseMsg);
								response.write(responseStr);

							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case STOP:
							System.out.println("Stop watching...");
							try {
								watchDir.interrupt();
								responseMsgData = new MessageData(
										ResponseStatus.OK,
										CommandType.STOP,
										"Stop watching...");
								responseMsg = new Message(
										MessageType.RESPONSE,
										responseMsgData);
								responseStr = gson.toJson(responseMsg);
								response.write(responseStr);
							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						case DISCONNECT:
							try {
								responseMsgData = new MessageData(
										ResponseStatus.OK,
										CommandType.DISCONNECT,
										"Disonnected");
								responseMsg = new Message(
										MessageType.RESPONSE,
										responseMsgData);
								responseStr = gson.toJson(responseMsg);
								response.write(responseStr);
							} catch (Exception e) {
								e.printStackTrace();
							}
							isRunning = false;
							break;
						default:
							System.out.println("Invalid command");
							break;
					}

					response.newLine();
					response.flush();
				}

				response.close();
				request.close();
				socket.close();
				if (watchDir != null)
					watchDir.interrupt();
			}
		} catch (ConnectException e) {
			System.out.println("Server is not running");
		} catch (SocketException e) {
			System.out.println("Connection closed");
			if (watchDir != null)
				watchDir.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void print(String event, String path) {
		MessageData messageData = new MessageData(event, path);
		Message message = new Message(MessageType.EVENT, messageData);
		Gson gson = new Gson();
		String messageStr = gson.toJson(message);
		try {
			response.write(messageStr);
			response.newLine();
			response.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
