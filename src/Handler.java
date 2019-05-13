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
				while ((numChars = inServer.read(reply, 0, reply.length)) != -1) {
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
				
						
				try {
					
					host = fileLocationCache.get(doc);
					if (host == null) {
						finalOutput.append("Cache miss");
						host = replicationServer.getIP(doc);
						fileLocationCache.put(doc, host);
					}
					
					finalOutput.append("Host is: " + host + "\n");
					//System.out.println("Host is: " + host);
					server = new Socket(host, 8505);
					server.setSoTimeout(3000);
					inServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
					outServer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
					ServerThread serverThread = new ServerThread(host, server, inServer, outClient);
					serverThread.start();
					NumThreads++;
					finalOutput.append("Number of active threads: " + java.lang.Thread.activeCount() + "\n");
					//System.out.println(java.lang.Thread.activeCount());
					outServer.write(request, 0, numChars);
					outServer.flush();
					System.out.println(finalOutput.toString());
					finalOutput = new StringBuilder();			
					
				} catch (UnknownHostException e) {
						e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
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
