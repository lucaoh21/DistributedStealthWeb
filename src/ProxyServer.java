import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Queue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class ProxyServer {
	
	private String INDEX_PATH = "../system_config/dist-index.txt";
	private ServerSocket ServerSocket;
	private static String REPLICATION_MANAGER_HOST = "54.209.66.61";
	private boolean isRunning;
	private static LRUCache FileLocationCache;
	
	public ProxyServer(int port) {
		try {
			ServerSocket = new ServerSocket(port);
			isRunning = true;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void listen (RmiServerIntf replicationServer, LRUCache FileLocationCache) {
		
		while (isRunning) {
			try {
				Socket socket = ServerSocket.accept();
				System.out.println("New connection made with " +  socket.getInetAddress() + ":" + socket.getLocalPort());
				//System.out.println("new connection accepted");
				
				Thread newThread = new Thread(new Handler(socket, replicationServer, FileLocationCache));

				newThread.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		
		int port = 8080;
		String local = "localhost";
		FileLocationCache = new LRUCache(2);
		
		RmiServerIntf replicationServer;
		try {
			Registry registry = LocateRegistry.getRegistry("54.209.66.61", 8099);
		        replicationServer = (RmiServerIntf) registry.lookup("RepServer");
			ProxyServer proxy = new ProxyServer(port);
			System.out.println(replicationServer.getMessage());
			proxy.listen(replicationServer, FileLocationCache);
			
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
		}
	
		
	}

}
