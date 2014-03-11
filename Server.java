import java.io.*;
import java.net.*;


public class Server implements Runnable{
	
	private ServerSocket serverSocket;
	protected static Thread receiveThread;
	protected static Thread sendThread;
	protected static Server server;
	protected static Client client;
	protected static RSA rsa = new RSA();
	protected static Boolean stopSending = false;
	
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
		try{
			
			// Computes new keys each time you run
			int[] keys = rsa.computeKeys();
			int myPublicKey = keys[1];
			int myC = keys[0];
			int myPrivateKey = keys[2];
			
			// Output the keys
			System.out.println("Server: Your public key is (" + myPublicKey + ", " + myC + ")");
			System.out.println("Server: Your private key is (" + myPrivateKey + ", " + myC + ")");
			
			while(true){
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket sock = serverSocket.accept(); // Accept new connection
				System.out.println("Connected to " + sock.getRemoteSocketAddress());
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
				while(true){
					try{
						String message = in.readLine();
						if(message.equals("\\q")){ // The agreed upon quit input
							stopSending = true;
							break;
						}
						else{
							String[] array = message.split(" "); // The message comes in as 123 456 789, so split it
							System.out.println(message);
							for(int i=0; i<array.length-1; i++){
								int value = (int) Double.parseDouble(array[i]);
								System.out.print(rsa.decrypt(value, myPrivateKey, myC)); // Decrypt character and print it
							}
							System.out.println();		
						}
					} catch(NullPointerException e){
						break;
					} catch(NumberFormatException e){
						e.printStackTrace();
					}
				}
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
		int socket = 2012;
		if(args.length > 1 || args.length == 0){
			System.err.println("Too many arguments passed, should only pass the port you wish to open");
		}
		else{
			socket = Integer.parseInt(args[0]);
		}

		// Create the threads and start them
		client = new Client();
		server = new Server(socket);
		Thread clientThread = new Thread(client);
		Thread serverThread = new Thread(server);
		clientThread.start();
		serverThread.start();
		
		// Join to the threads
		try {
			clientThread.join();
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// This is responsible for encryption and sending messages
	private static class Client implements Runnable{
		private static int theirPublicKey;
		private static int theirKeyC;
		private static Socket s;

		public Client(){}
		
		@Override
		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			while(true){
				try {
					// Prompt for the IP address
					System.out.println("Client: Please input the IP address of the person you wish to connect to: ");
					String input = br.readLine();
					InetAddress addr = InetAddress.getByName(input);
					
					// Prompt for the port number
					System.out.println("Client: Please input the port number: ");
					int port = Integer.parseInt(br.readLine());
					
					// Make the connection
					s = new Socket(addr, port);
					System.out.println("Connected to " + s.getRemoteSocketAddress() + " on port " + s.getPort());
					
					// Input of partner's public key
					System.out.println("Client: Please input your partner's public key separated by a space. For example: 123 456: ");
					String[] keys = br.readLine().split(" ");
					theirPublicKey = Integer.parseInt(keys[0]);
					theirKeyC = Integer.parseInt(keys[1]);
					System.out.println("You may now type your messages");
					
					// New thread for actually sending the messages
					Thread sender = new Thread(new MessageSender(s));
					sender.start();
					try {
						sender.join(); // Join to the sending thread
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
		
		private static class MessageSender implements Runnable{
			private Socket sock;
			
			public MessageSender(Socket sock){
				this.sock = sock;
			}
			
			@Override
			public void run() {
				try{
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					
					while(!s.isClosed() && !stopSending){
						String message = in.readLine();
						if(message.equals("\\q")){
							out.writeUTF(message);
							out.writeUTF("\n");
							s.close(); // Close the socket
							break;
						}
						else{
							// Break the message into characters, encrypt each character, add it to a string, and send the whole thing
							char[] array = message.toCharArray();
							StringBuilder sb = new StringBuilder();
							for(int i=0; i<array.length; i++){
								sb.append(Integer.toString(rsa.encrypt(array[i], theirPublicKey, theirKeyC)) + " ");
							}
							out.writeUTF(sb.toString());
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
	}

}