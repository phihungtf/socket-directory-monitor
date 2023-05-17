package thread;

import java.io.*;
import java.net.*;
import java.util.List;

import com.google.gson.Gson;

import file.DirectoryExplorer;
import message.Message;
import message.MessageData;
import message.Message.MessageType;
import message.MessageData.CommandType;

public class SocketThread extends Thread {
	private Socket socket;
	private PrintStream ps;
	BufferedReader request;
	BufferedWriter response;

	public SocketThread(Socket socket, PrintStream ps) {
		this.socket = socket;
		this.ps = ps;
	}

	public void run() {
		Gson gson = new Gson();
		try {
			System.out.println(
					"New client connected: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

			request = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			response = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			String requestStr, responseStr;
			Message requestMsg, responseMsg;
			MessageData requestMsgData, responseMsgData;
			do {
				responseMsgData = new MessageData(CommandType.GET_DRIVES, "");
				responseMsg = new Message(MessageType.REQUEST, responseMsgData);
				response.write(gson.toJson(responseMsg));
				response.newLine();
				response.flush();

				requestStr = request.readLine();
				ps.println(requestStr);
				responseMsg = gson.fromJson(requestStr, Message.class);
				requestMsgData = responseMsg.getData();
				List<String> drives = DirectoryExplorer.stringToArray(requestMsgData.getData());
				ps.println(drives);

				responseMsgData = new MessageData(CommandType.GET_DIRS, drives.get(1));
				responseMsg = new Message(MessageType.REQUEST, responseMsgData);
				response.write(gson.toJson(responseMsg));
				response.newLine();
				response.flush();

				requestStr = request.readLine();
				ps.println(requestStr);
				responseMsg = gson.fromJson(requestStr, Message.class);
				requestMsgData = responseMsg.getData();
				List<String> dirs = DirectoryExplorer.stringToArray(requestMsgData.getData());
				ps.println(dirs);
				break;
			} while (true);
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		response.close();
		request.close();
		ps.flush();
		ps.close();
		socket.close();
	}
}