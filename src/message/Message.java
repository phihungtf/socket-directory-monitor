package message;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
	public enum MessageType {
		REQUEST, RESPONSE, EVENT
	}

	private MessageType type;
	private MessageData data;
	private String timestamp;

	public Message(MessageType type, MessageData data) {
		this.type = type;
		this.data = data;
		this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
	}

	public MessageType getType() {
		return type;
	}

	public MessageData getData() {
		return data;
	}

	public String getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Message [type=" + type + ", data=" + data + ", timestamp=" + timestamp + "]";
	}
}
