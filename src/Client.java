import file.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.*;

import watcher.*;

import com.google.gson.*;

import message.*;
import message.Message.MessageType;
import message.MessageData.CommandType;
import message.MessageData.ResponseStatus;

public class Client {
	public static void main(String[] args) throws Exception {
		GsonBuilder builder = new GsonBuilder();
		// builder.setPrettyPrinting();
		Gson gson = builder.create();

		DirectoryExplorer dirEx = new DirectoryExplorer();

		try {
			BufferedReader request;
			BufferedWriter response;

			try (Socket socket = new Socket("localhost", 6969)) {
				System.out.println("Connected to server: " + socket.getInetAddress().getHostAddress() + ":"
						+ socket.getPort());

				request = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				response = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				String requestStr, responseStr;
				Message requestMsg, responseMsg;
				MessageData requestMsgData, responseMsgData;
				while (true) {
					requestStr = request.readLine();
					System.out.println(requestStr);

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
									DirectoryExplorer
											.arrayToString(DirectoryExplorer.getDirectories(requestMsgData.getData())));
							responseMsg = new Message(
									MessageType.RESPONSE,
									responseMsgData);
							responseStr = gson.toJson(responseMsg);
							response.write(responseStr);
							break;
						case GO_UP:

							break;
						case GO_DOWN:

							break;
						case LIST_FILES:

							break;
						case CHANGE_DRIVE:

							break;
						case WATCH:

							break;
						default:
							System.out.println("Invalid command");
							break;
					}

					response.newLine();
					response.flush();

					// register directory and process its events
					// boolean recursive = true;
					// Path dir = Paths.get("C:/");
					// new WatchDir(dir, recursive).processEvents();
					// break;
				}

				response.close();
				request.close();
				socket.close();
			}
		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}
}
