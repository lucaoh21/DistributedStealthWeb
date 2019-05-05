import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
public class RepServer implements Hello {
	private static RepServer obj;
	public RepServer() {}
	public String sayHello() { return "Hello, world!"; }
	public	static void main(String args[]) {
		try {
			obj = new RepServer();
			Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 8887);
			Registry registry = LocateRegistry.createRegistry(8887);
			registry.bind("Hello", stub);
 		} catch (Exception e) { 
			System.err.println("Server exception: " + e.toString()); 
		}
 	}
}
