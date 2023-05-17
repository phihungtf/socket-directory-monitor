package message;

import com.google.gson.annotations.SerializedName;

public class MessageData {
	public enum CommandType {
		GET_DRIVES, GET_DIRS, GO_UP, GO_DOWN, LIST_FILES, CHANGE_DRIVE, WATCH
	}

	public enum ResponseStatus {
		OK, ERROR
	}

	@SerializedName("command")
	private CommandType command;
	@SerializedName("status")
	private ResponseStatus status;
	@SerializedName("event")
	private String event;
	@SerializedName("data")
	private String data;

	public MessageData(CommandType command, String data) {
		this.command = command;
		this.data = data;
	}

	public MessageData(ResponseStatus status, String data) {
		this.status = status;
		this.data = data;
	}

	public MessageData(String event, String data) {
		this.event = event;
		this.data = data;
	}

	public CommandType getCommand() {
		return command;
	}

	public ResponseStatus getStatus() {
		return status;
	}

	public String getEvent() {
		return event;
	}

	public String getData() {
		return data;
	}

	@Override
	public String toString() {
		return "MessageData [command=" + command + ", status=" + status + ", event=" + event + ", data=" + data + "]";
	}
}
