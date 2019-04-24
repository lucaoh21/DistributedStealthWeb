import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ProxyServer {
	
	private ServerSocket serverSocket;
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
	
	public void listen () {
		
		while(isRunning) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("new connection accepted");
				
				Thread newThread = new Thread(new Handler(socket));
				newThread.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		
		int port = 8080;
		
		ProxyServer proxy = new ProxyServer(port);
		proxy.listen();
		
	}

}
