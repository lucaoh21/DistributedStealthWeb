//NOTE: USED START UP CODE FROM: https://en.wikipedia.org/wiki/Java_remote_method_invocation, 
//http://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html#d5e49
// util
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.lang.Float;

// map and streaming
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

// RMI
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

// client
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

/**
 * @author DylanHR
 *
 */

public class RmiServer implements RmiServerIntf {
	public static final String MESSAGE = "Hello World";
	private static final String INDEX_PATH = "../system_config/dist-index.txt";
	private static final String HOST_POOL_PATH = "../system_config/ip-list.txt";
	private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();
	private static final String SPAWN_CMD = "../scripts/system_commands/spawn/spawn_node.sh ";
	private static final String FILE_MOVE_CMD = "../scripts/system_commands/spawn/inter_server_scp.sh ";
	private static final String START_CMD = "../scripts/system_commands/start_node.sh ";
	
	//mapping of document filename to list of servers storing document
	private static HashMap<String, ArrayList<String>> indexMap;
	//mapping of server to list of files it stores
	private static HashMap<String, ArrayList<String>> hostMap;
	//mapping of server to health
	private static HashMap<String, String> hostPool;
	private static RmiServer rmi;

	public RmiServer() throws RemoteException {
	}
	
	/**
	 * spawnNode: Runs a set of scripts from the library to spawn a node on the given host, 
	 * copy over the correct documents, and start both the proxy and httpd servers
	 * @param host
	 * @param docs
	 * @return process_exit_code
	 */
	private static int spawnNode(String host, ArrayList<String> docs) {
		int result = -1;
		
		// copy and build source
		try {
			Process p = Runtime.getRuntime().exec(SPAWN_CMD + host);
			
			// monitor progress
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
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(result);
			return result;
		}
		
		// copy documents
		for (String doc : docs) {
			String source = "localhost";
			if (indexMap.containsKey(doc)) {
				source = indexMap.get(doc).get(0);
			} else {
				System.out.println("Lost Doc: " + doc);
				continue;
			}
			try {
				Process p = Runtime.getRuntime().exec(FILE_MOVE_CMD + source + " " + host + " " + doc.replace("/", ""));
				result = p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Move files: " + Integer.toString(result));
				return result;
			}
		}
		
		// start server
		try {
			Process p = Runtime.getRuntime().exec(START_CMD + host);
			result = p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Start Command: " + Integer.toString(result));
			return result;
		}
		System.out.println("Final Exit Code: " + Integer.toString(result));
		return result;
	}
	
	/**
	 * LoadResources: Loads the initial INDEX and HOSTS mapping from disk.
	 *...STEALTH_WEB_HOME/system_config/dist-index.txt.
	 */
	private static void loadResources() {
		indexMap = new HashMap<String, ArrayList<String>>();
		hostMap = new HashMap<String, ArrayList<String>>();
		FileReader file;
		try {
			file = new FileReader(INDEX_PATH);
			BufferedReader buf_reader = new BufferedReader(file);
			String line;
			System.out.println("Load INDEX and HOSTS...");
			while ((line = buf_reader.readLine()) != null) {
				String[] tokens = line.split("\\s");
				if (tokens.length != 2) {
					System.out.println(Arrays.toString(tokens));
					System.out.println("error reading distributed index!");
					System.exit(1);
				} else {
					if (indexMap.containsKey(tokens[0])) {
						indexMap.get(tokens[0]).add(tokens[1]);
					} else {
						indexMap.put(tokens[0], new ArrayList<String>());
						indexMap.get(tokens[0]).add(tokens[1]);
					}
					if (hostMap.containsKey(tokens[1])) {
						hostMap.get(tokens[1]).add(tokens[0]);
					} else {
						hostMap.put(tokens[1], new ArrayList<String>());
						hostMap.get(tokens[1]).add(tokens[0]);
					}

				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * LoadPool: Load the entire host pool from disk. STEALTH_WEB_HOME/system_config/ip-list.txt.
	 */
	private static void loadPool() {
		hostPool = new HashMap<String, String>();
		FileReader file;
		try {
			file = new FileReader(HOST_POOL_PATH);
			BufferedReader buf_reader = new BufferedReader(file);
			String line;
			System.out.println("load host pool");
			
			while ((line = buf_reader.readLine()) != null) {
				if (!hostPool.containsKey(line)) {
					hostPool.put(line, "healthy");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * pickHost: Picks a new, unused host from the HOST_POOL. Exludes all hosts in the pickedHosts
	 * list, as well as all hosts in the HOSTS HashMap.
	 * @param pickedHosts
	 * @return String newHost or "NO HOSTS LEFT"
	 */
	public static String pickHost(ArrayList<String> pickedHosts) {
		for (String newHost : hostPool.keySet()) {
			if (!hostMap.containsKey(newHost) && !pickedHosts.contains(newHost)) {
				pickedHosts.add(newHost);
				return newHost;
			}
		}
		return "NO HOSTS LEFT";
	}
	
	/**
	 * ping: Uses the CloseAbleHTTPCLient, to ping a specific ip. The function requests the index of the web
	 * server. If the code returned is not 200 or their is an exception, the ping returns as "unhealthy".
	 * Otherwise, the ping returns "healthy" and the RRT for the request.
	 * @param ip
	 * @return String[] status {health, RRT for request}
	 */
	private static String[] ping(String ip) {
		
		// build and send response
		try {
			URI uri = new URIBuilder().setScheme("http").setHost(ip).setPath("/").build();
			HttpGet httpget = new HttpGet("http://" + ip + ":8505/");
			
			long startTime = System.nanoTime();
			CloseableHttpResponse response = HTTP_CLIENT.execute(httpget);
			
			// attempt to extract status code
			try {
				long endTime = System.nanoTime();
				long duration = (endTime - startTime) / 1000000; // Milliseconds
				response.close();
				System.out.println(response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {
					return new String[] { "healthy", Long.toString(duration) };
				} else {
					return new String[] { "unhealthy", Long.toString(duration) };
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (URISyntaxException e) {
			System.out.println("Syntax Exception");
		} catch (HttpHostConnectException e) {
			System.out.println("Host Exception");
		} catch (IOException e) {
			System.out.println("Io Exception");
		}
		return new String[] { "unhealthy", "unknown" };
	}
	
	/**
	 * redoIndex: This method is responsible for reconfiguring the index after a ping sweep.
	 * While pingBackend pings each server, it will start up a new thread, spawning a node on that
	 * server. It records each modification it makes to the service and sends these modification to 
	 * redoIndex. If a single host is in one mod, then the most is just removed. If multiple, then
	 * the first replaces the second
	 * @param mods, list of service modifications 
	 */
	private static synchronized void redoIndex(ArrayList<String[]> mods) {
		// reconfigure HOSTS
		for (String[] mod : mods) {
			if (mod.length == 2) {
				hostMap.put(mod[0], hostMap.get(mod[1]));
				hostMap.remove(mod[1]);
			} else if (mod.length == 1) {
				hostMap.remove(mod[0]);
			}
		}
		
		// reconfigure INDEX
		for (String[] mod : mods) {
			if (mod.length == 2) {
				for (String doc : indexMap.keySet()) {
					if (indexMap.get(doc).contains(mod[1])) {
						indexMap.get(doc).remove(mod[1]);
						indexMap.get(doc).add(mod[0]);
					}
				}
			} else if (mod.length == 1) {
				for (String doc : indexMap.keySet()) {
					if (indexMap.get(doc).contains(mod[0])) {
						indexMap.get(doc).remove(mod[-1]);
					}
				}
			}
		}
		System.out.println("Index Reconfigured");
	}
	
	/**
	 * pingAll: send a ping to every host in HOST_POOL.
	 */
	private static void pingAll() {
		for (String host : hostPool.keySet()) {
			ping(host);
		}
	}

	/**
	 * respawn: Managers the flow for the respawn mechanic. 
	 * @param old_host: the old_host being replaced/
	 * @param mods: the list of modification to the system.
	 * @param pickedHosts: the hosts already picked for spawn in this ping sweep.
	 */
	private static void respawn(String old_host, ArrayList<String[]> mods, ArrayList<String> pickedHosts) {
		String newHost = pickHost(pickedHosts);
		System.out.println("spawning on " + newHost);
		int result = spawnNode(newHost, hostMap.get(old_host));
		if (result == 0) {
			mods.add(new String[] { newHost, old_host });
		} else {
			System.out.println("spawn failed");
			mods.add(new String[] { old_host });
		}
	}

	/**
	 * pringBackend: pings each web server, if one fails it starts a new thread respawning that server.
	 * Repairs index. Prints out the INDEX and HOSTS maps.
	 * @param v: bool, print status info
	 */
	private static void pingBackend(boolean v) {
		ArrayList<String[]> mods = new ArrayList<String[]>();
		ArrayList<SpawnThread> threads = new ArrayList<SpawnThread>();
		ArrayList<String> pickedHosts = new ArrayList<String>();
		long startTime = System.nanoTime();
		
		for (String old_host : hostMap.keySet()) {
			if (v) {
				System.out.print("Pinging " + old_host + "...");
			}
			
			String[] status = ping(old_host);
			if (hostPool.containsKey(old_host)) {
				hostPool.put(old_host, status[0]);
				if (status[0] == "unhealthy") {
					SpawnThread re = new SpawnThread(old_host, mods, pickedHosts);
					re.start();
					threads.add(re);
				}
			} else {
				System.out.println("FOUND A MYSTERY HOST: " + old_host);
			}
		}
		System.out.println("waiting for spawning threads");
		
		for (SpawnThread re : threads) {
			try {
				re.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		redoIndex(mods);
		long endTime = System.nanoTime();
		long dur = (endTime - startTime) / 1000000;
		if (v) {System.out.println("Spawn duration for " + Integer.toString(threads.size()) + "servers: " + Long.toString(dur));}
		
		for (String host : hostMap.keySet()) {
			if (v) {System.out.println(host + " -> " + Arrays.toString(hostMap.get(host).toArray()));}
		}
		
		for (String doc : indexMap.keySet()) {
			if (v) {System.out.println(doc + " -> " + Arrays.toString(indexMap.get(doc).toArray()));}
		}
		if (v) {
			if (v) {System.out.println("Backend Ping Complete!");}
		}

	}
	
	/**
	 * getMessage: RMI method, for testing connection to RMI server.
	 */
	public String getMessage() throws RemoteException {
		return MESSAGE;
	}

	/**
	 * getIP: RMI method, get ip with the specified key. Picks a random ip from set
	 */
	public String getIP(String key) throws RemoteException {
		Random rand = new Random();

		if (!indexMap.containsKey(key)) {
			List<String> keys = new ArrayList<String>(indexMap.keySet());
			key = keys.get(rand.nextInt(keys.size()));

		} else {
			key = null;
		}

		return key;
	}

	/**
	 * SpawnThread: a Thread for concurrently spawning new hosts. Essentially a wrapper for respawn()
	 * @author DylanHR
	 *
	 */
	static class SpawnThread extends Thread {
		String old_host;
		ArrayList<String[]> mods;
		ArrayList<String> pickedHosts;

		SpawnThread(String old_host, ArrayList<String[]> mods, ArrayList<String> pickedHosts) {
			this.old_host = old_host;
			this.mods = mods;
			this.pickedHosts = pickedHosts;
		}

		public void run() {
			respawn(old_host, mods, pickedHosts);
		}
	}

	/**
	 * PingThread: Repeatedly pings backend, every three seconds.
	 * @author DylanHR
	 *
	 */
	static class PingThread extends Thread {

		PingThread() {
		}

		public void run() {
			while (true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				pingBackend(true);
			}
		}
	}

	/*
	 * Thread for testing system, ignore. Times spawning host on server.
	 */
	static class TestThread extends Thread {
		String host;

		TestThread(String host) {
			this.host = host;
		}

		public void run() {
			System.out.println("Spawning " + host);
			long start = System.nanoTime();
			spawnNode(host, hostMap.get("3.94.170.64"));
			long end = System.nanoTime();
			System.out.print(host + " ");
			System.out.println((end - start) / 1000000);
		}
	}

	/*
	 * Main method: loads resources, starts up RMI server, starts ping thread.
	 */
	public static void main(String args[]) throws Exception {
		System.out.println("RMI server started");
		loadResources();
		loadPool();
		rmi = new RmiServer();
		Registry registry = LocateRegistry.createRegistry(8099);
		RmiServerIntf stub = (RmiServerIntf) UnicastRemoteObject.exportObject(rmi, 8096);

		registry.bind("RepServer", stub);
		
		System.out.println("RepServer bound in registry");
		PingThread pt = new PingThread();
		pt.start();
	}
}
