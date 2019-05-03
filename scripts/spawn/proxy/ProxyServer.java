import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class ProxyServer {
	
	private String INDEX_PATH = "dist-index.txt";
	private ServerSocket serverSocket;
	private boolean isRunning;
	private HashMap<String,String> dist_index;
	
	public ProxyServer(int port) {
		load_dist_index();
		try {
			serverSocket = new ServerSocket(port);
			isRunning = true;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	private void load_dist_index() {
		dist_index = new HashMap<String,String>();
		dist_index.put("/", "54.209.66.61");
		FileReader file;
		try {
			file = new FileReader(INDEX_PATH);
			BufferedReader buf_reader = new BufferedReader(file);
			String line;
			System.out.println("load");
			while((line = buf_reader.readLine()) != null){
				String[] tokens = line.split("\\s");
				if (tokens.length != 2){
					System.out.println("error reading distributed index!");
					System.exit(1);
				} else {
					dist_index.put(tokens[0], tokens[1]);
				}
			}
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		} 
	}
	
	public void listen () {
		
		while(isRunning) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("new connection accepted");
				
				Thread newThread = new Thread(new Handler(socket, dist_index));
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
