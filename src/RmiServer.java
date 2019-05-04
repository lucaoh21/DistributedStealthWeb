//NOTE: USED START UP CODE FROM: https://en.wikipedia.org/wiki/Java_remote_method_invocation, 
//http://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html#d5e49
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Float;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*; 

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

//import org.apache.http.client.methods.*; 
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.conn.HttpHostConnectException;
//import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.apache.http.client.utils.URIBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.InterruptedException;


public class RmiServer extends UnicastRemoteObject implements RmiServerIntf {
    public static final String MESSAGE = "Hello World";
    private static HashMap<String, ArrayList<String>> INDEX;
    private static HashMap<String,String> HOSTS;
    private static String INDEX_PATH = "../system_config/dist-index.txt";
//    private static CloseableHttpClient HTTP_CLIENT =  HttpClients.createDefault();
    
    public RmiServer() throws RemoteException {
        super(0);    // required to avoid the 'rmic' step, see below
    }

    private static void loadResources() {
    	INDEX = new HashMap<String, ArrayList<String>>();
    	HOSTS = new HashMap<String,String>();
		//INDEX.put("/", "54.209.66.61");
		FileReader file;
		try {
			file = new FileReader(INDEX_PATH);
			BufferedReader buf_reader = new BufferedReader(file);
			String line;
			System.out.println("load");
			while ((line = buf_reader.readLine()) != null) {
				String[] tokens = line.split("\\s");
				if (tokens.length != 2) {
					System.out.println(Arrays.toString(tokens));
					System.out.println("error reading distributed index!");
					System.exit(1);
				} else {
					ArrayList<String> hostNames = new ArrayList<String>();
					//starts at 1 since element 0 is the filename
					for (int i = 1; i < tokens.length; i++) {
						hostNames.add(tokens[i]);
					}
					
					INDEX.put(tokens[0], hostNames);
					HOSTS.put(tokens[1], "healthy");

				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

    public String getMessage() throws RemoteException {
        return MESSAGE;
    }
    
//    private static String[] ping(String ip){
//    	try {
//    		URI uri = new URIBuilder()
//    				.setScheme("http")
//    				.setHost(ip)
//    				.setPath("/")
//    				.build();
//    		HttpGet httpget = new HttpGet("http://"+ip+":8500/");
//    		long startTime = System.nanoTime();
//    		CloseableHttpResponse response = HTTP_CLIENT.execute(httpget);
//    		long endTime = System.nanoTime();
//
//    		long duration = (endTime - startTime)/1000000;
//
//        	try {
//        		response.close();
//        		if (response.getStatusLine().getStatusCode() == 200) {
//        			return new String[] {"healthy", Float.toString(duration)};
//        		} else {
//        			return new String[] {"unhealthy", Float.toString(duration)};
//        		}
//        	} catch (IOException e) {
//        		e.printStackTrace();
//        	}
//        	
//    	} catch (URISyntaxException e) {
//    		e.printStackTrace();
//    	} catch (HttpHostConnectException e) {
//    		return new String[] {"unhealthy", "unknown"};
//    	} catch (IOException e) {
//    		e.printStackTrace();
//    	} 
//    	
//    	
//    	return new String[] {"unhealthy", "unknown"};
//    }
    
//    private static void pingBackend(boolean v){
//    	for (String key: HOSTS.keySet()) {
//    		if (v) {
//    			System.out.print("Pinging " + key + "...");
//    		}
//    		String[] status = ping(key);
//    		HOSTS.put(key, status[0]);
//    		if (v) {
//    			System.out.print(status[0] + " " + status[1] + " ms");
//    		}
//    		if (v) {
//    			System.out.println();
//    		}
//    	}
//
//    	if (v) {
//    		System.out.println("Backend Ping Complete!");
//    	}
//    	
//    }
    
    public String getIP(String key) throws RemoteException{
		Random rand = new Random();

    	if (!INDEX.containsKey(key)) {
    		List<String> keys = new ArrayList<String>(INDEX.keySet());
    		key = keys.get(rand.nextInt(keys.size()));
    	}
    	
    	int size = INDEX.get(key).size();
    	System.out.println("Size is: " + size);
    	return INDEX.get(key).get(rand.nextInt(size));
    	
//    	Random       random    = new Random();
//    	List<String> keys      = new ArrayList<String>(x.keySet());
//    	String       randomKey = keys.get( random.nextInt(keys.size()) );
//    	String       value     = x.get(randomKey);
//    	
//    	Random generator = new Random();
//    	Object[] values = myHashMap.values().toArray();
//    	Object randomValue = values[generator.nextInt(values.length)];
    	
    }
    
//    static class PingThread extends Thread {
//
//
//		PingThread(){
//		}
//
//		public void run() {
//			while(true){
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e){
//					e.printStackTrace();
//				} 
//				pingBackend(true);
//			}
//		}
//	}
    
    public static void main(String args[]) throws Exception {
        System.out.println("RMI server started");
        loadResources();

        try { //special exception handler for registry creation
            LocateRegistry.createRegistry(1099); 
            System.out.println("java RMI registry created.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println("java RMI registry already exists.");
        }
           
        //Instantiate RmiServer

        RmiServer obj = new RmiServer();

        // Bind this object instance to the name "RmiServer"
        Naming.rebind("//localhost/RmiServer", obj);
        System.out.println("PeerServer bound in registry");
//        PingThread pt = new PingThread();
//        pt.start();
    }
}
