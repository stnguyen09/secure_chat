package com.securechat.client;
import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.securechat.message.HandshakeMessage;
import com.securechat.message.Message;
import com.securechat.message.MessageContainer;
import com.securechat.message.MessageType;
import com.securechat.util.Encryption;


public class ClientNode implements Runnable{
	
	private static Socket socket;
	private static Map<String, Key> trustedBuddies;
	private static String name;
	private static KeyPair keys;
	private static boolean handshakeMode = false;
	
	// This is responsible for receiving and decrypting messages
	public ClientNode(String hostName, int socketNumber, String clientName){
		try{
			name = clientName;
			InetAddress addr = InetAddress.getByName(hostName);
			socket = new Socket(addr, socketNumber);
			trustedBuddies = new HashMap<String, Key>();
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
		System.out.println("Server: Your public key is (" + keys.getPublic().getEncoded() + ")");
		System.out.println("Server: Your private key is (" + keys.getPrivate().getEncoded() + ")");

		while(true){
			try {
				System.out.println("Connected to " + socket.getRemoteSocketAddress() + " on port " + socket.getPort());
				
				// Initial handshake
				BufferedReader handShakeReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				HandshakeMessage initialServerHS = HandshakeMessage.createHandshakeMessage(MessageType.HANDSHAKE, null, name, keys.getPublic().getEncoded());
				String handShakeJson = initialServerHS.getJSON();
				
				System.out.println(handShakeJson);
				
				out.writeUTF(handShakeJson);
				
				// Blocks until the server responds
				String response = handShakeReader.readLine();
				HandshakeMessage hsResponse = null;
				
				try{
					hsResponse = new Gson().fromJson(response, HandshakeMessage.class);
				} catch(JsonSyntaxException e){
					System.err.println("Invalid json format. Handshake failed. Exiting.");
					System.exit(0);
				}
				
				if(hsResponse.getMessageType() == MessageType.SERVER_STATUS && hsResponse.getSource().equals("server")){
					
					// New thread for actually sending messages
					Thread sender = new Thread(new ClientMessageSender());
					Thread receiver = new Thread(new ClientMessageReceiver());
					sender.start();
					receiver.start();
					try {
						sender.join(); // Join to the sending thread
						receiver.start(); // Join to the receiver thread
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			} catch (UnknownHostException u){
				System.err.println("Client: Unable to find host for address");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException n){
				System.out.println("Client: Invalid integer for the port number");
			} catch (NullPointerException e){
				break;
			}
		}
	}
		
	private static class ClientMessageSender implements Runnable{
		
		public ClientMessageSender(){}
		
		@Override
		public void run() {
			try{
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				
				// TODO: Handshake
				
				while(!socket.isClosed()){
					String data = in.readLine();
					if(data.equals("\\q")){
						// TODO: Send a quit message to the server if necessary
						
						out.writeUTF("\n");
						socket.close(); // Close the socket
						break;
					}
					else{
						Message message = new Message();
						byte[] encryptedData = Encryption.encryptMessage(data, Encryption.generateRandomKey(), "AES");

						
						
						out.writeUTF("\n");
					}
				}
			} catch(SocketTimeoutException to){
				System.out.println("Socket timed out");
			} catch(SocketException e){
				System.out.print("Connection closed");
			} 
			catch(IOException e){
				e.printStackTrace();
			} 
		}		
	}
	
	private static class ClientMessageReceiver implements Runnable{
		
		public ClientMessageReceiver(){}
		
		@Override
		public void run() {
			while(true){
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					
					String receivedData = in.readLine();
					
					Object receivedMessage = null;
					try{
						if(receivedData.contains(MessageType.HANDSHAKE.toString())){
							handshakeMode = true;
							receivedMessage = new Gson().fromJson(receivedData, HandshakeMessage.class);
							
						}
						else if(receivedData.contains(MessageType.MESSAGE.toString())){
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
							BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
							System.out.println("Incoming handshake from < " + handshakeMessage.getSource() + " >. Would you like to accept? Y/N");
							
							String response = userInputReader.readLine();
							if(response.toLowerCase().equals("y")){
								// Get their public key and put it into the trusted buddies array
								Key publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(handshakeMessage.getKey()));
								trustedBuddies.put(handshakeMessage.getSource(), publicKey);
								
								DataOutputStream handshakeOutput = new DataOutputStream(socket.getOutputStream());
								HandshakeMessage outputHandshake = HandshakeMessage.createHandshakeMessage(MessageType.HANDSHAKE, handshakeMessage.getSource(), name, keys.getPublic().getEncoded());
								
								handshakeOutput.writeUTF(outputHandshake.getJSON());
								// Public key sent, handshake complete
							}
						}
						else if(receivedMessage instanceof MessageContainer){
		
							String stringMessage = ((MessageContainer) receivedMessage).getMessage().toString();
							
							// Decrypt the Message in the MessageContainer
							byte[] decryptedBytes = Encryption.decryptMessage(stringMessage, keys.getPrivate(), "RSA");
							
							ByteArrayInputStream bis = new ByteArrayInputStream(decryptedBytes);
							ObjectInputStream ois = new ObjectInputStream(bis);
							Message message = (Message) ois.readObject();
							
							byte[] payload = message.getPayload();
							String source = ((MessageContainer) receivedMessage).getSource();
							Key randomKey = Encryption.decryptKey(message.getRandomKey(), trustedBuddies.get(source));
							
							String decryptedPayload = Encryption.decryptMessage(payload.toString(), randomKey, "AES").toString();
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
				}		
			}
			
		}
	
	}

}