import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Handler implements Runnable {
	
	private Socket socket;
	private Socket server;
	private BufferedReader inClient;
	private BufferedWriter outClient;
	private BufferedReader inServer;
	private BufferedWriter outServer;

	public Handler(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		
		char[] request = new char[1024];
		char[] reply = new char[4096];
		
		String input;
		try {
			inClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			input = inClient.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			server = new Socket("54.209.66.61", 8500);
			inServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
			outServer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//handling client to server connection
		new Thread() {
			public void run()  {
				try {
					int numChars;
					while((numChars = inClient.read(request, 0, request.length)) != -1) {
						outServer.write(request, 0, numChars);
						outServer.flush();
					}
					
					//end of stream reached
					if (numChars == -1) {
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			}
		}.start();
		
		
		//parse the input and respond to user
		
	}
}



