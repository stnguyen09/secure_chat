package com.securechat.message;

public class MessageContainer {

	private byte[] message;
	private String source;
	private String destination;
	private MessageType messageType;
	
	public MessageContainer(byte[] message, String source, String destination, MessageType messageType){
		this.message = message;
		this.source = source;
		this.destination = destination;
		this.messageType = messageType;
	}
	
	public byte[] getMessage(){
		return this.message;
	}
	
	public void setMessage(byte[] message){
		this.message = message;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
}
