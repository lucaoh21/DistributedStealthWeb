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

//here

public class Handler implements Runnable {

	private Socket client;
	private Socket server;
	private BufferedReader inClient;
	private BufferedWriter outClient;
	private BufferedReader inServer;
	private BufferedWriter outServer;
	private Pattern FILE_REGEX = Pattern.compile("/[\\d\\w]*[.html]*");
	private int NumThreads = 0;
	private RmiServerIntf replicationServer;
	private StringBuilder finalOutput = new StringBuilder();
	private LRUCache fileLocationCache;

	public Handler(Socket client, RmiServerIntf replicationServer, LRUCache fileLocationCache) {

		this.client = client;
		this.replicationServer = replicationServer;
		this.fileLocationCache = fileLocationCache;
	}

	class ServerThread extends Thread {
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
			this.reply = new char[4096];
		}

		public void run() {
			int numChars;
			try {
				StringBuilder testString = new StringBuilder();
				while ((numChars = inServer.read(reply, 0, reply.length)) != -1) {
					for(int i = 0; i < reply.length; i++) {
						testString.append(reply[i]);
					}
					System.out.println("Request: " + testString.toString());
					outClient.write(reply, 0, numChars);
					outClient.flush();
				}
			} catch (SocketTimeoutException e) {
				finalOutput.append("Exception: socket timeout\n");
				//System.out.println("socket timedout");
			} catch (IOException e){
				System.out.println("io exception");
			} finally {
				try {
					finalOutput.append("Connection with server " + server.getInetAddress() + " closed\n");
					server.close();
					//System.out.println("server closed");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	@Override
	public void run() {
		
		char[] request = new char[1024];
		
		// set up client streams
		try {
			inClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		// thread handling client to proxy connection
	
		try {
			int numChars;
					
			while((numChars = inClient.read(request, 0, request.length)) != -1) {
						
//				for(int i = 0; i < request.length; i++) {
//						
//					System.out.print(request[i]);
//				}
						
				// get doc and ip of machine with doc
				//System.out.println("matching");
				finalOutput.append("Matching requested file with server.");
				String req_string = new String(request);
				Matcher m = FILE_REGEX.matcher(req_string);
				m.find();
				String doc = m.group();
				//String doc = String.copyValueOf(m.group().toCharArray(), 1, m.group().length()-1);
				//System.out.println("Doc is: " + doc);
				finalOutput.append("Filename is: " + doc + "\n");
				String host;
				
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
						if (host == null || numIOExceptions > 0) {
							finalOutput.append("Cache miss\n");
							host = replicationServer.getIP(doc);
							fileLocationCache.put(doc, host);
							numIOExceptions += 1;
						}
						
						//the requested document is not available
						if (host == null) {
							String notFoundMessage = "<html><head>\n" + 
									"<title>404 Not Found</title>\n" + 
									"</head><body>\n" + 
									"<h1>Not Found</h1>\n" + 
									"<p>The requested URL " + doc + " was not found on this server.</p>\n" + 
									"</body></html>\n";
							//char[] response = notFoundMessage.toCharArray();
							outClient.write(notFoundMessage);
						} else {
							System.out.println(fileLocationCache.printMap());
							
							finalOutput.append("Host is: " + host + "\n");
							server = new Socket(host, 8505);
							server.setSoTimeout(3000);
							
							inServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
							outServer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
							ServerThread serverThread = new ServerThread(host, server, inServer, outClient);
							serverThread.start();
							NumThreads++;
							finalOutput.append("Number of active threads: " + java.lang.Thread.activeCount() + "\n");
							
							outServer.write(request, 0, numChars);
							outServer.flush();
							System.out.println(finalOutput.toString());
							finalOutput = new StringBuilder();		
						}
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
				//System.out.println("client closed");
			} catch (IOException e){
				e.printStackTrace();
			}
		}
					
		
	}
}
