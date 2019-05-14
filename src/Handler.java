import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Queue; 
import java.lang.String;
import java.util.HashMap;
import java.io.FileReader;
import java.rmi.Naming;
import java.rmi.RemoteException;

/*
 * A handler class that gets created as a new thread by the ProxyServer for
 * each client. The handler takes care of communication between the client and
 * the backend server. It utilizes sockets to establish a connection. It then reads
 * in a request, parses it, and calls the Replication Manager if the file location is not
 * stored in the cache. The handler then requests the file from the backend server and passes
 * it along to the client.
 */ 
public class Handler implements Runnable {

	//used to determine what file is being requested
	private static final Pattern FILE_REGEX = Pattern.compile("/[\\d\\w]*[.html]*");
	private static final int SERVER_PORT = 8505;
	private static final int TIMEOUT_LENGTH = 3000;
	private static final int REPLY_LENGTH = 4096;
	private static final int REQUEST_LENGTH = 1024;
	
	//connection to the client and the backend server
	private Socket client;
	private Socket server;
	//used for communication in and out from client and server
	private BufferedReader inClient;
	private BufferedWriter outClient;
	private BufferedReader inServer;
	private BufferedWriter outServer;
	
	//tracking the number of threads running
	private int NumThreads = 0;
	
	//reference to the Replication Manager and cache from the proxy
	private RmiServerIntf replicationServer;
	private LRUCache fileLocationCache;
	
	//accumulates messages and outputs them as one when request is serviced
	private StringBuilder finalOutput = new StringBuilder();


	public Handler(Socket client, RmiServerIntf replicationServer, LRUCache fileLocationCache) {
		this.client = client;
		this.replicationServer = replicationServer;
		this.fileLocationCache = fileLocationCache;
	}

	/*
	 * Thread that is spawned before request is passed to server, in order to handle the response 
	 * from the server back to the client. Thread closes when there is nothing else to read.
	 */
	class ServerThread extends Thread {
		//the IP of the backend server to contact
		String host;
		Socket server;
		BufferedReader inServer;
		BufferedWriter outClient;
		char[] reply;


		ServerThread(String host, Socket server, BufferedReader inServer, BufferedWriter outClient){
			this.host = host;
			this.server = server;
			this.inServer = inServer;
			this.outClient = outClient;
			this.reply = new char[REPLY_LENGTH];
		}

		public void run() {
			int numChars;
			
			try {
				//reads messages from server and writes them out to the client
				while ((numChars = inServer.read(reply, 0, reply.length)) != -1) {
					outClient.write(reply, 0, numChars);
					outClient.flush();
				}
				
			} catch (SocketTimeoutException e) {
				finalOutput.append("Exception: socket timeout\n");
			} catch (IOException e){
				System.out.println("io exception");
			} finally {
				
				try {
					finalOutput.append("Connection with server " + server.getInetAddress() + " closed\n");
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * The 'main' method for the handler class. This method sets up input and output to the client
	 * and the server. After the request from the client has been read in, it attempts to find the
	 * file location in the cache. If this fails, it makes an RPC to the Replication Manager for a
	 * location. It then spawns a thread to handle the response from the server and writes out the 
	 * request to the server.
	 */
	@Override
	public void run() {
		
		char[] request = new char[REQUEST_LENGTH];
		
		// set up client streams
		try {
			inClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//handles client to proxy connection
		try {
			int numChars;
			while((numChars = inClient.read(request, 0, request.length)) != -1) {
				//parse request to get document name
				String req_string = new String(request);
				Matcher m = FILE_REGEX.matcher(req_string);
				m.find();
				String doc = m.group();
				finalOutput.append("Filename is: " + doc + "\n");
				String host = null;
				
				/*
				 * System attempts to use file location stored in cache. If that fails, because the
				 * location has been changed, then an IOException will be thrown. In that case the
				 * system queries the RMIServer for another location and updates the cache. If an
				 * IOException is thrown using the RMI given location, the system does not loop again.
				 */
				int numIOExceptions = 0;
				while(numIOExceptions < 2) {
					try {
						host = fileLocationCache.get(doc);
						//if cache failed, use Replication Manager
						if (host == null || numIOExceptions > 0) {
							finalOutput.append("Cache miss\n");
							host = replicationServer.getIP(doc);
							fileLocationCache.put(doc, host);
							numIOExceptions += 1;
						}
						
						System.out.println(fileLocationCache.printMap());
						
						finalOutput.append("Host is: " + host + "\n");
						server = new Socket(host, SERVER_PORT);
						server.setSoTimeout(TIMEOUT_LENGTH);
						inServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
						outServer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
						ServerThread serverThread = new ServerThread(host, server, inServer, outClient);
						serverThread.start();
						NumThreads++;
						finalOutput.append("Number of active threads: " + java.lang.Thread.activeCount() + "\n");
						
						//write request to the server
						outServer.write(request, 0, numChars);
						outServer.flush();
						
						//print out the complete output
						System.out.println(finalOutput.toString());
						finalOutput = new StringBuilder();		
						
						break;
						
					} catch (UnknownHostException e) {
							e.printStackTrace();
							break;
					} catch (IOException e) {
						numIOExceptions += 1;
						e.printStackTrace();
					} 
				}			
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
					
			// close connection
			try {
				finalOutput.append("Connection with client " + client.getInetAddress() + " closed\n");
				client.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}
