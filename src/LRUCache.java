import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/*
 * This class implements an LRU style cache using a LinkedHashMap. The cache
 * has a maximum size set by the user and evicts the oldest entry automatically
 * if there are too many entries.
 */
public class LRUCache {
	
	private final Map<String, String> cache;
	
	public LRUCache(int maxEntries) {
		cache = new LinkedHashMap<String, String>(maxEntries, 0.75F, true) {

			private static final long serialVersionUID = 1L;

			/*
			 * Override the remove method to only remove if we have more than maxEntries.
			 */
			@Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest){            
               return size() > maxEntries;
            }
		};
		
	}
	
	public String get(String key) {
		synchronized(cache) {
			return cache.get(key);
		}
	}
	
	public void put(String key, String value) {
		synchronized(cache) {
			cache.put(key, value);
		}
	}
	
	/*
	 * Prints out the keys and values of a cache.
	 */
	public String printMap() {

		StringBuilder string = new StringBuilder();
			
		for (Entry<String, String> key : cache.entrySet()) {
			string.append(key + ", ");
		}
		if(string.length() != 0) {
			return string.substring(0, string.length()-2).toString();
		}
		return string.toString();
	}

}
