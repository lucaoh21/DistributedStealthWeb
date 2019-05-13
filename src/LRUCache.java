import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LRUCache {
	
	private final Map<String, String> cache;
	
	public LRUCache(int maxEntries) {
		cache = new LinkedHashMap<String, String>(maxEntries, 0.75F, true) {

			private static final long serialVersionUID = 1L;

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
	
	public String printMap() {

		StringBuilder string = new StringBuilder();
			
		for (Entry<String, String> key : cache.entrySet()) {
			string.append(key + ", ");
		}
		return string.substring(0, string.length()-2).toString();
	}

}
