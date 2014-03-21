package com.securechat.server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.securechat.message.HandshakeMessage;
import com.securechat.message.MessageType;


public class Server implements Runnable{
	
	private static ServerSocket serverSocket;
	protected static Thread receiveThread;
	protected static Thread sendThread;
	protected static Server server;
	protected static Boolean stopSending = false;
	private static ConcurrentHashMap<Socket, String> activeConnections;
	private static List<String> uniqueActiveNames;
	
	// This is responsible for receiving and decrypting messages
	public Server(int socket){
		try{
			serverSocket = new ServerSocket(socket);
		} catch(IOException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void run(){
		activeConnections = new ConcurrentHashMap<Socket, String>();
		uniqueActiveNames = Collections.synchronizedList(new ArrayList<String>());
		
		try{			
			while(true){
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket sock = serverSocket.accept(); // Accept new connection
				System.out.println("Connected to " + sock.getRemoteSocketAddress());
				
				// Add to the list of online people. Doesn't have a name yet until a handshake occurs.
				activeConnections.putIfAbsent(sock, "");
				
				Thread clientHandlerThread = new Thread(new ClientHandler(sock));
				clientHandlerThread.start();
			}
		} catch(SocketTimeoutException to){
			System.out.println("Socket timed out");
		} catch(IOException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static class ClientHandler implements Runnable{

		private Socket clientSocket;
		
		public ClientHandler(Socket sock){
			clientSocket = sock;
		}
		
		@Override
		public void run() {
			try {
				DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
				
				// Listen for handshake from this socket
				String handshakeString = in.readUTF();
				JsonParser parser = new JsonParser();
				JsonObject handshakeObj = parser.parse(handshakeString).getAsJsonObject();
				
				try{
					HandshakeMessage handshakeMessage = new Gson().fromJson(handshakeObj, HandshakeMessage.class);
					String name = handshakeMessage.getSource();
					
					if(!uniqueActiveNames.contains(name)){
						uniqueActiveNames.add(name);
						// Associate the name with the socket
						activeConnections.putIfAbsent(clientSocket, name);
					}
					else{
						System.err.println("Duplicate name detected: " + name + ". Ignoring!");
						return;
					}
					
					handshakeMessage.setMessageType(MessageType.SERVER_STATUS);
					handshakeMessage.setSource("server");
					/*
					 *  Protocol specific thing, the server responds to a handshake using 
					 *  MessageType.SERVER_STATUS and putting the online people into the destination field.
					 */
					handshakeMessage.setDestination(this.uniqueActiveNamesString());
					
					// Send the response. Handshake complete.
					out.writeUTF(new Gson().toJson(handshakeMessage));
					out.writeUTF("\n");
				} catch(Exception e){
					// If we fail the handshake, close the thread
					// Any exception thrown here counts as a failure. Clean up and return.
					System.err.println("Client handshake failed.");
					throw e; // Rethrow to trigger the finally block that cleans up
				}
				
				while(!clientSocket.isClosed()){
					System.out.println(in.readUTF());
					// TODO: Implement me!
					
					// Listen for messages using in.readLine()
					
					// When you get it, parse it, convert to object if necessary
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					// Clean up
					clientSocket.close();
					String name = activeConnections.get(clientSocket);
					uniqueActiveNames.remove(name);
					activeConnections.remove(clientSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			// TODO: close the socket, remove from available clients, kill thread
		}
		
		private void sendMessage(String message){
		
			// TODO: Implement
			
		}
		
		private String receiveMessage(){
			
			// TODO: Implement
			
			return null;
		}
		
		private void routeMessage(){
			
			// TODO: Implement
			
		}
		
		// Returns the list as a comma-separated string
		private String uniqueActiveNamesString(){
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < uniqueActiveNames.size(); i++){
				builder.append(uniqueActiveNames.get(i));
				if(i < (uniqueActiveNames.size()-1)){
					builder.append(",");
				}
			}
			
			return builder.toString();
		}
	}
}