package com.securechat.message;

import com.google.gson.Gson;

public class HandshakeMessage {

	private MessageType messageType;
	private String destination;
	private String source;
	private byte[] key;
	
	private HandshakeMessage() {}
	
	private HandshakeMessage(MessageType type, String destination, String source, byte[] key){
		this.setMessageType(type);
		this.setDestination(destination);
		this.setSource(source);
		this.setKey(key);
	}
	
	public static HandshakeMessage createHandshakeMessage(MessageType type, String destination, String source, byte[] key){
		if(isValidMessageType(type)){
			return new HandshakeMessage(type, destination, source, key);
		}
		
		return null;
	}
	
	private static boolean isValidMessageType(MessageType type){
		if(type == MessageType.HANDSHAKE || type == MessageType.HANDSHAKE_RESPONSE || type == MessageType.SERVER_STATUS){
			return true;
		}
		else{
			System.err.println("Wrong message type.");
			return false;
		}
	}
	
	public String getJSON(){
		return new Gson().toJson(this);
	}
	
	public MessageType getMessageType(){
		return this.messageType;
	}
	
	public void setMessageType(MessageType type){
		if(isValidMessageType(type)){
			this.messageType = type;
		}
	}
	
	public String getDestination(){
		return this.destination;
	}
	
	public void setDestination(String destination){
		this.destination = destination;
	}
	
	public String getSource(){
		return this.source;
	}
	
	public void setSource(String source){
		this.source = source;
	}
	
	public byte[] getKey(){
		return this.key;
	}
	
	public void setKey(byte[] key){
		this.key = key;
	}
	
}
