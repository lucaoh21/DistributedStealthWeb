import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Handler implements Runnable {
	
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	public Handler(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		
		String input;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			input = in.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//parse the input and respond to user
		
	}
}



