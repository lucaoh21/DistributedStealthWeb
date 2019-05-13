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
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.http.client.methods.*; 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.utils.URIBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.InterruptedException;


public class RmiServer implements RmiServerIntf {
    private static RmiServer obj;
    public static final String MESSAGE = "Hello World";
    private static HashMap<String, ArrayList<String>> INDEX;
    private static HashMap<String, ArrayList<String>> HOSTS;
    private static HashMap<String, String> HOST_POOL;
    private static String INDEX_PATH = "../system_config/dist-index.txt";
    private static String HOST_POOL_PATH = "../system_config/ip-list.txt";
    private static CloseableHttpClient HTTP_CLIENT =  HttpClients.createDefault();
    private static String SPAWN_CMD = "../scripts/system_commands/spawn/spawn_node.sh ";
    private static String FILE_MOVE_CMD = "../scripts/system_commands/spawn/inter_server_scp.sh ";
    private static String START_CMD = "../scripts/system_commands/start_node.sh ";
    public RmiServer() throws RemoteException {
            // required to avoid the 'rmic' step, see below
    }
    private static int spawnNode(String host, ArrayList<String> docs){
	int result = -1;
	try {	
		Process p = Runtime.getRuntime().exec(SPAWN_CMD + host);
	       	BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		int count = 0;
        	String line = "";
		String total_lines = "6008";
        	while ((line = reader.readLine()) != null) {
        		System.out.print(count);
			System.out.print("/"+total_lines +"\r");
			count++;
        	}
		result = p.waitFor();
		System.out.println("Node built on " + host);
		System.out.println(result);
    	} catch (Exception e){
		e.printStackTrace();
		return result;
	}
	for (String doc : docs){
		String source = "localhost";
		if (INDEX.containsKey(doc)){
			source = INDEX.get(doc).get(0);
		} else {
			System.out.println("Lost Doc: " + doc);
			continue;
		}
		try { 
			System.out.println(FILE_MOVE_CMD + source + " " + host + " " + doc.replace("/", ""));
			Process p = Runtime.getRuntime().exec(FILE_MOVE_CMD + source + " " + host + " " + doc.replace("/", ""));
                	BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                	String line = "";
                	while ((line = reader.readLine()) != null) {
                        	System.out.println(line);
                	}
                	result = p.waitFor();
			System.out.println(result);
		}catch (Exception e){
			e.printStackTrace();
			return result;
		}
	}
	try {
		System.out.println(START_CMD + host);
		Process p = Runtime.getRuntime().exec(START_CMD + host);
		result = p.waitFor();
		System.out.println("Started node on " + host);
		System.out.println(result);
        }catch (Exception e){
                        e.printStackTrace();
                        return result;
        }
    	return result;
    }
    private static void loadResources() {
    	INDEX = new HashMap<String, ArrayList<String>>();
    	HOSTS = new HashMap<String,ArrayList<String>>();
		FileReader file;
		try {
			file = new FileReader(INDEX_PATH);
			BufferedReader buf_reader = new BufferedReader(file);
			String line;
			System.out.println("load index and reverse index");
			while ((line = buf_reader.readLine()) != null) {
				String[] tokens = line.split("\\s");
				if (tokens.length != 2) {
					System.out.println(Arrays.toString(tokens));
					System.out.println("error reading distributed index!");
					System.exit(1);
				} else {
					if (INDEX.containsKey(tokens[0])){
						INDEX.get(tokens[0]).add(tokens[1]);
					} else {
						INDEX.put(tokens[0], new ArrayList<String>());
						INDEX.get(tokens[0]).add(tokens[1]);
					}
					if (HOSTS.containsKey(tokens[1])){
                                                HOSTS.get(tokens[1]).add(tokens[0]);
                                        } else {
                                                HOSTS.put(tokens[1], new ArrayList<String>());
						HOSTS.get(tokens[1]).add(tokens[0]);
                                        }
		

				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	public static String pickHost(ArrayList<String> pickedHosts){
		for (String newHost : HOST_POOL.keySet()){
			if (!HOSTS.containsKey(newHost) && !pickedHosts.contains(newHost)){
				pickedHosts.add(newHost);
				return newHost;
			}
		}
		return "NO HOSTS LEFT";	
	}	
	private static void loadPool() {
        	HOST_POOL = new HashMap<String, String>();
                FileReader file;
                try {
                        file = new FileReader(HOST_POOL_PATH);
                        BufferedReader buf_reader = new BufferedReader(file);
                        String line;
                        System.out.println("load host pool");
                        while ((line = buf_reader.readLine()) != null) {
                        	if (!HOST_POOL.containsKey(line)){
					HOST_POOL.put(line, "healthy");
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
	
    
    private static String[] ping(String ip){
    
	try {
    		URI uri = new URIBuilder()
    				.setScheme("http")
    				.setHost(ip)
    				.setPath("/")
    				.build();
    		HttpGet httpget = new HttpGet("http://"+ip+":8505/");
    		long startTime = System.nanoTime();
    		CloseableHttpResponse response = HTTP_CLIENT.execute(httpget);
    		long endTime = System.nanoTime();

    		long duration = (endTime - startTime)/1000000;
        	try {
        		response.close();
        		System.out.println(response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 200) {
        			return new String[] {"healthy", Float.toString(duration)};
        		} else {
        			return new String[] {"unhealthy", Float.toString(duration)};
        		}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	
    	} catch (URISyntaxException e) {
    		System.out.println("syntax exception");
    	} catch (HttpHostConnectException e) {
		System.out.println("Host exception");
		return new String[] {"unhealthy", "unknown"};
    	
	} catch (IOException e) {
		System.out.println("io exception");
    	} 
    	
    	
    	return new String[] {"unhealthy", "unknown"};
    }
    

    private static synchronized void redoIndex(ArrayList<String[]> mods){
	for (String[] mod : mods){
		if (mod.length == 2){
			HOSTS.put(mod[0], HOSTS.get(mod[1]));
			HOSTS.remove(mod[1]);
		} else if (mod.length == 1){
			HOSTS.remove(mod[0]);
		}
	}
	for (String[] mod : mods){
		if (mod.length == 2){ 
		        for (String doc : INDEX.keySet()){
				if (INDEX.get(doc).contains(mod[1])){
					INDEX.get(doc).remove(mod[1]);
					INDEX.get(doc).add(mod[0]);
				}
			}
                } else if (mod.length == 1){
                        for (String doc : INDEX.keySet()){
                                if (INDEX.get(doc).contains(mod[0])){
                                        INDEX.get(doc).remove(mod[-1]);
                                }
                        }
                }
	}
	System.out.println("index reconfigured");
    }


    private static void respawn(String key,ArrayList<String[]> mods, ArrayList<String> pickedHosts){
         String newHost = pickHost(pickedHosts);
         System.out.println("spawning on " + newHost);
         int result = spawnNode(newHost, HOSTS.get(key));
         if (result == 0) {
         	mods.add(new String[] {newHost, key});
         } else{
                System.out.println("spawn failed");
                mods.add(new String[] {key});
         }
    }
    
    private static void pingBackend(boolean v){
	ArrayList<String[]> mods = new ArrayList<String[]>();
	ArrayList<SpawnThread> threads = new ArrayList<SpawnThread>();
    	ArrayList<String> pickedHosts = new ArrayList<String>();
	for (String key: HOSTS.keySet()) {
    		if (v) {
    			System.out.print("Pinging " + key + "...");
    		}
    		String[] status = ping(key);
		if (HOST_POOL.containsKey(key)){
			HOST_POOL.put(key, status[0]);
			if (status[0] == "unhealthy"){
                		SpawnThread re = new SpawnThread(key, mods, pickedHosts);
        			re.start();
				threads.add(re);	
			}
		} else {
			System.out.println("FOUND A MYSTERY HOST: " + key);
		}
    	}
	System.out.println("waiting for spawning threads");
	for (SpawnThread re : threads){
		try {
			re.join();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	redoIndex(mods);
	for (String host : HOSTS.keySet()){
		 System.out.println(host + " -> " + Arrays.toString(HOSTS.get(host).toArray()));
	}
	for (String doc : INDEX.keySet()){
		System.out.println(doc + " -> " + Arrays.toString(INDEX.get(doc).toArray()));
	}
    	if (v) {
    		System.out.println("Backend Ping Complete!");
    	}
    	
    }
    
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
    static class SpawnThread extends Thread {
		String key;
		ArrayList<String[]> mods;
		ArrayList<String> pickedHosts;
                SpawnThread(String key, ArrayList<String[]> mods, ArrayList<String> pickedHosts){
                	this.key = key;
			this.mods = mods;
			this.pickedHosts = pickedHosts;
		}

                public void run() {
                	respawn(key, mods, pickedHosts);
		}
        }   
    static class PingThread extends Thread {


		PingThread(){
		}

		public void run() {
			while(true){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e){
					e.printStackTrace();
				} 
				pingBackend(true);
			}
		}
	}
    
    public static void main(String args[]) throws Exception {
    	System.out.println("RMI server started");
    	loadResources();
	loadPool();
	obj = new RmiServer();
    	Registry registry = LocateRegistry.createRegistry(8099);
    	RmiServerIntf stub = (RmiServerIntf) UnicastRemoteObject.exportObject(obj, 8096);
        
                
        //Instantiate RmiServer

        //RmiServer obj = new RmiServer();

        // Bind this object instance to the name "RmiServer"
        registry.bind("RepServer", stub); 
        //Naming.rebind("//:8097/RmiServer", obj);
        System.out.println("RepServer bound in registry");
        
	PingThread pt = new PingThread();
        pt.start();
    }
}
