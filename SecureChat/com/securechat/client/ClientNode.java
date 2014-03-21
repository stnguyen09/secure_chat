package com.securechat.client;
import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.securechat.message.HandshakeMessage;
import com.securechat.message.Message;
import com.securechat.message.MessageContainer;
import com.securechat.message.MessageType;
import com.securechat.util.Encryption;


public class ClientNode implements Runnable{
	
	private static Socket socket;
	private static ConcurrentHashMap<String, Key> trustedBuddies;
	private static List<String> onlineBuddies;
	private static String name;
	private static KeyPair keys;
	private static boolean handshakeMode;
	
	// This is responsible for receiving and decrypting messages
	public ClientNode(String hostName, int socketNumber, String clientName){
		try{
			name = clientName;
			InetAddress addr = InetAddress.getByName(hostName);
			socket = new Socket(addr, socketNumber);
			trustedBuddies = new ConcurrentHashMap<String, Key>();
			handshakeMode = false;
			onlineBuddies = Collections.synchronizedList(new ArrayList<String>());
		} catch(IOException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void run(){		
		// Computes new keys each time you run
		keys = Encryption.generateKeyPair();
		
		// Output the keys
		// TODO: Remove this
		System.out.println("Your public key is (" + keys.getPublic().getEncoded() + ")");
		System.out.println("Your private key is (" + keys.getPrivate().getEncoded() + ")");

		while(true){
			try {
				System.out.println("Connected to " + socket.getRemoteSocketAddress() + " on port " + socket.getPort());
				
				// Initial handshake
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream());
				
				
				HandshakeMessage initialServerHS = HandshakeMessage.createHandshakeMessage(MessageType.HANDSHAKE, null, name, keys.getPublic().getEncoded());
				String handShakeJson = initialServerHS.getJSON();
				
				System.out.println(handShakeJson);
				out.writeUTF(handShakeJson);
				
				// Blocks until the server responds
				String response = in.readUTF();
				HandshakeMessage hsResponse = null;
				
				JsonParser parser = new JsonParser();
				JsonObject handshakeObj = parser.parse(response).getAsJsonObject();

				try{
					hsResponse = new Gson().fromJson(handshakeObj, HandshakeMessage.class);
				} catch(JsonSyntaxException e){
					System.err.println("Invalid json format. Handshake failed. Exiting.");
					System.exit(0);
				}
				
				if(hsResponse.getMessageType() == MessageType.SERVER_STATUS && hsResponse.getSource().equals("server")){
					// Populate the list of online people
					onlineBuddies = new ArrayList<String>(Arrays.asList(hsResponse.getDestination().split(",")));
					
					System.out.println(onlineBuddies.size());
					
					// New thread for actually sending messages
					Thread sender = new Thread(new ClientMessageSender());
					Thread receiver = new Thread(new ClientMessageReceiver());
					sender.start();
					receiver.start();
					try {
						sender.join(); // Join to the sending thread
						receiver.join(); // Join to the receiver thread
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (UnknownHostException u){
				System.err.println("Client: Unable to find host for address");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException n){
				System.out.println("Client: Invalid integer for the port number");
			} catch (NullPointerException e){
				e.printStackTrace();
				break;
			} 
		}
		
		// Close and exit
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private static class ClientMessageSender implements Runnable{
		
		public ClientMessageSender(){}
		
		@Override
		public void run() {
			try{
				System.out.println("I successfully handshaked.");
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				
				while(!socket.isClosed()){
					String data = in.readLine();
					if(data.equals("\\q")){
						// Send quit message to the server so it's knows you're gone
						out.writeUTF("quit");
						socket.close(); // Close the socket
						break;
					}
					else if(data.equals("\\status")){
						// Request the list of online users and update it
						out.writeUTF("status");
						// Wait for server response
						String statusResponse = new DataInputStream(socket.getInputStream()).readUTF();
						
						String[] onlineArray = statusResponse.split(",");
						for(String buddy : onlineArray){
							if(!onlineBuddies.contains(buddy)){
								onlineBuddies.add(buddy);
							}
						}
					}
					else if(data.equals("\\handshake")){
						System.out.println("Which buddy do you want to handshake with?: ");
						String handshakee = in.readLine();
						
						if(!onlineBuddies.contains(handshakee)){
							System.out.println("That person is not online...");
						}
						else{
							if(trustedBuddies.containsKey(handshakee)){
								System.out.println("You already shook hands with this person...");
							}
							else{
								// Send a request containing your public key
								HandshakeMessage buddyHandshake = HandshakeMessage.createHandshakeMessage(MessageType.HANDSHAKE, handshakee, name, keys.getPublic().getEncoded());
								out.writeUTF(new Gson().toJson(buddyHandshake));
							}
						}
					}
					else{
						if(!handshakeMode){
							for(Map.Entry<String, Key> entry : trustedBuddies.entrySet()){
								if(!onlineBuddies.contains(entry.getKey())){
									// If the buddy isn't online, remove him from trustedBuddies and move on
									trustedBuddies.remove(entry.getKey());
									continue;
								}
								
								Key randomKey = Encryption.generateRandomKey();
								
								// Encrypt the message payload
								byte[] encryptedPayload = Encryption.encryptMessage(data.getBytes(), randomKey, "AES");
								
								// Encrypt the random key with the other person's public key
								byte[] encryptedRandomKey = Encryption.encryptKey(randomKey, entry.getValue());
								
								Message messageToSend = new Message(encryptedPayload, encryptedRandomKey);
								
								// Encrypt the Message object json with your private key
								byte[] encryptedMessage = Encryption.encryptMessage(new Gson().toJson(messageToSend).getBytes(), keys.getPrivate(), "RSA");

								MessageContainer messageContainerToSend = new MessageContainer(encryptedMessage, name, entry.getKey(), MessageType.MESSAGE);
								
								// Send to the server
								out.writeUTF(new Gson().toJson(messageContainerToSend));
							}
						}
					}
				}
			} catch(SocketTimeoutException to){
				System.out.println("Socket timed out");
			} catch(SocketException e){
				System.out.print("Connection closed");
			} catch(IOException e){
				e.printStackTrace();
			} 
		}		
	}
	
	private static class ClientMessageReceiver implements Runnable{
		
		public ClientMessageReceiver(){}
		
		@Override
		public void run() {
			while(!socket.isClosed()){
				try {
					DataInputStream in = new DataInputStream(socket.getInputStream());
					
					String receivedData = in.readUTF();
					JsonParser parser = new JsonParser();
					JsonObject handshakeObj = parser.parse(receivedData).getAsJsonObject();
					
					Object receivedMessage = null;
					try{
						if(handshakeObj.get("MessageType").equals(MessageType.HANDSHAKE)){
							handshakeMode = true;
							receivedMessage = new Gson().fromJson(receivedData, HandshakeMessage.class);
							
						}
						else if(handshakeObj.get("MessageType").equals(MessageType.MESSAGE)){
							handshakeMode = false;
							receivedMessage = new Gson().fromJson(receivedData, Message.class);
						}
					} catch(JsonSyntaxException e){
						e.printStackTrace();
					}
					
					if(receivedMessage == null){
						// Something got messed up, quit this iteration and listen again
						handshakeMode = false;
						continue;
					}
					else{
						// If the message received was a handshake request from another user
						if(receivedMessage instanceof HandshakeMessage){
							HandshakeMessage handshakeMessage = (HandshakeMessage) receivedMessage;
							
							// It's a response to a handshake you sent
							if(handshakeMessage.getMessageType() == MessageType.HANDSHAKE_RESPONSE){
								
							}
							
							BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
							System.out.println("Incoming handshake from < " + handshakeMessage.getSource() + " >. Would you like to accept? Y/N");
							
							String response = userInputReader.readLine();
							
							// Respond to the handshake request
							if(response.toLowerCase().equals("y")){
								// Get their public key and put it into the trusted buddies array
								Key publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(handshakeMessage.getKey()));
								trustedBuddies.putIfAbsent(handshakeMessage.getSource(), publicKey);
								
								DataOutputStream handshakeOutput = new DataOutputStream(socket.getOutputStream());
								HandshakeMessage outputHandshake = HandshakeMessage.createHandshakeMessage(MessageType.HANDSHAKE_RESPONSE, handshakeMessage.getSource(), name, keys.getPublic().getEncoded());
								
								handshakeOutput.writeUTF(outputHandshake.getJSON());
								// Public key sent, handshake complete
							}
						}
						else if(receivedMessage instanceof MessageContainer){
							byte[] byteMessage = ((MessageContainer) receivedMessage).getMessage();
							String source = ((MessageContainer) receivedMessage).getSource();
							
							// Decrypt the Message in the MessageContainer using own other person's public key (auth that they sent it)
							byte[] decryptedBytes = Encryption.decryptMessage(byteMessage, trustedBuddies.get(source), "RSA");

							// Transform the byte stream to an object
							ByteArrayInputStream bis = new ByteArrayInputStream(decryptedBytes);
							ObjectInputStream ois = new ObjectInputStream(bis);
							Message message = (Message) ois.readObject();
							
							// Get the payload
							byte[] payload = message.getPayload();
							
							// Unwrap the random key using your own private key (ensures only you can decrypt it)
							Key randomKey = Encryption.decryptKey(message.getRandomKey(), keys.getPrivate());
							
							// Decrypt and output the string in the payload
							String decryptedPayload = Encryption.decryptMessage(payload, randomKey, "AES").toString();
							System.out.println(source + "> " + decryptedPayload);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					handshakeMode = false;
				}
			}
			
		}
	
	}

}