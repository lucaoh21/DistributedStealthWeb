import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

public class ProxyServer {
	
	private String INDEX_PATH = "../system_config/dist-index.txt";
	private ServerSocket serverSocket;
	private static String REPLICATION_MANAGER_HOST = "54.209.66.61";
	private boolean isRunning;
	
	public ProxyServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
			isRunning = true;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void listen (RmiServerIntf replicationServer) {
		
		while (isRunning) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("new connection accepted");
				
				Thread newThread = new Thread(new Handler(socket, replicationServer));

				newThread.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		
		int port = 8080;
		String local = "localhost";
		RmiServerIntf replicationServer;
		try {
			replicationServer = (RmiServerIntf) Naming.lookup("//"+ REPLICATION_MANAGER_HOST+ ":8099/RmiServer");
														
			ProxyServer proxy = new ProxyServer(port);
			proxy.listen(replicationServer);
			
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
		}
	
		
	}

}
