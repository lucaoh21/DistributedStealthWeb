import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class TestClient {
    private TestClient() {}
    public static void main(String[] args) {
	String host = (args.length < 1) ? "localhost" : args[0];
	try {
	    Registry registry = LocateRegistry.getRegistry("54.209.66.61", 8887);
	    Hello stub = (Hello) registry.lookup("Hello");
	    System.out.println("getting response");
	    String response = stub.sayHello();
	    System.out.println("response: " + response);
	} catch (Exception e) { System.err.println("Client exception: " + e.toString()); }
    }
}