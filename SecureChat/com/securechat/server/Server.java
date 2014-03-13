package com.securechat.server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Server implements Runnable{
	
	private ServerSocket serverSocket;
	protected static Thread receiveThread;
	protected static Thread sendThread;
	protected static Server server;
	protected static Boolean stopSending = false;
	private static List<Socket> activeConnections;
	
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
		activeConnections = Collections.synchronizedList(new ArrayList<Socket>());
		
		try{			
			while(true){
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket sock = serverSocket.accept(); // Accept new connection
				System.out.println("Connected to " + sock.getRemoteSocketAddress());
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
				// Add to the list of online people
				activeConnections.add(sock);
				
				Thread clientHandlerThread = new Thread(new ClientHandler(sock));
				
				
			}
		} catch(SocketTimeoutException to){
			System.out.println("Socket timed out");
		} catch(IOException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/*
	 * args = {socket number}
	 */
	public static void main(String args[]) // args[0] should be the socket you want to listen on
	{
		int socket = 0;
		if(args.length > 1 || args.length == 0){
			System.err.println("Too many arguments passed, should only pass the port you wish to open");
			return;
		}
		else{
			socket = Integer.parseInt(args[0]);
		}
		
		// Create the threads and start them
		server = new Server(socket);
		Thread serverThread = new Thread(server);
		serverThread.start();
		
		// Join to the threads
		try {
			serverThread.join();
		} catch (InterruptedException e) {
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
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
				
				while(!clientSocket.isClosed()){
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	}
}