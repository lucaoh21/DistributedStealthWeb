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

/*
 * This class defines the proxy that is part of each node in the system.
 * After the proxy is contacted by a client, it spawns a handler thread
 * to allow concurrent requests to be processed. The proxy is also in charge
 * of maintaining an LRU style cache for document IP locations and a reference
 * to the Replication Manager.
 */
public class ProxyServer {
	//the IP of the Replication Manager
	private static final String REPLICATION_MANAGER_HOST = "54.209.66.61";
	private static final String REPLICATION_MANAGER_LOOKUP = "RepServer";
	private static final int REPLICATION_MANAGER_PORT = 8099;
	private static final int PROXY_PORT = 8080;

	private ServerSocket ServerSocket;
	private boolean isRunning;
	//a cache that stores the location of documents
	private static LRUCache FileLocationCache;
	
	public ProxyServer(int port) {
		try {
			//creates a server side socket for the proxy
			ServerSocket = new ServerSocket(port);
			isRunning = true;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * This function waits on a client to make a request and then spawns a new handler thread
	 * to take care of the request.
	 */
	public void listen (RmiServerIntf replicationServer, LRUCache FileLocationCache) {
		
		while (isRunning) {
			try {
				Socket socket = ServerSocket.accept();
				System.out.println("New connection made with " +  socket.getInetAddress() + ":" + socket.getLocalPort());
				
				Thread newThread = new Thread(new Handler(socket, replicationServer, FileLocationCache));
				newThread.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Function initializes the cache for the proxy and connects with the
	 * Replication Manager. It then calls the listening function to start
	 * accepting client requests.
	 */
	public static void main(String[] args) {
		
		FileLocationCache = new LRUCache(2);
		RmiServerIntf replicationServer;
		
		try {
			Registry registry = LocateRegistry.getRegistry(REPLICATION_MANAGER_HOST, REPLICATION_MANAGER_PORT);
			replicationServer = (RmiServerIntf) registry.lookup(REPLICATION_MANAGER_LOOKUP);
			ProxyServer proxy = new ProxyServer(PROXY_PORT);
			System.out.println(replicationServer.getMessage());
			proxy.listen(replicationServer, FileLocationCache);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
