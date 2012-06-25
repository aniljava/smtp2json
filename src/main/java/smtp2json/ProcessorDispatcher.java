package smtp2json;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import smtp2json.processors.Backup;
import smtp2json.processors.Logger;
import smtp2json.processors.PostJSON;

/**
 * Dispatches the incoming email to chain of different processors. Used by
 * Mailet.
 * 
 * @author Anil Pathak
 * 
 */
public class ProcessorDispatcher implements Processor {

	private Map<String, List<List<String>>>	processors		= new HashMap<>();
	private ObjectMapper					mapper			= new ObjectMapper();
	private String							folderName		= "config";
	private Map<String, Processor>			implementations	= new HashMap<>();
	public long								reloadInterval	= -1;

	public void createProcessorMap() {
		implementations.put(Logger.NAME, new Logger());
		implementations.put(Backup.NAME, new Backup());
		implementations.put(PostJSON.NAME, new PostJSON());

		// TODO Allow dynamically loading of processor
	}

	private long	lastReloaded	= 0;

	/**
	 * Checks if the configuration files needs to be reloaded.
	 */
	public void checkReload() {
		if (reloadInterval != -1 && reloadInterval + lastReloaded > System.currentTimeMillis()) {
			new Thread() {
				public void run() {
					try {
						reloadConfig();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}.start();
		}
	}

	/**
	 * Called by Mailet to check if given domain can be accepted.
	 * 
	 * @param domain
	 *            matching the domain name in the domain configuration file.
	 * @return true if the domain name entry exist or a wild card entry exist.
	 */
	public boolean accept(String domain) {
		checkReload();

		List items = processors.get(domain);

		if (items == null) {
			items = processors.get("*");
		}
		return (items != null);
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Object process(Object chainEnd, String domain, String from, String to, String id, List<String> args) throws Exception {

		List<List<String>> chain = processors.get(domain);
		if (chain.isEmpty()) {
			chain = processors.get("*");
		}

		for (List<String> list : chain) {
			String processorName = list.get(0);
			Processor processor = implementations.get(processorName);
			chainEnd = processor.process(chainEnd, domain, from, to, id, list);
		}

		if (chainEnd instanceof Closeable) {
			((Closeable) chainEnd).close();
		}
		// Nothing to return
		return null;
	}

	public void init(String folderName) throws Exception {
		this.folderName = folderName;
		createProcessorMap();
		reloadConfig();
	}

	public void reloadConfig() throws Exception {
		lastReloaded = System.currentTimeMillis();
		if (!new File(folderName).exists()) {
			return;
		}

		Map<String, List<List<String>>> tmp = new HashMap<>();

		File[] configs = new File(folderName).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});

		for (File file : configs) {
			try {
				Map<String, Object> data = mapper.readValue(file, Map.class);
				String domain = (String) data.get("domain");

				List<List<String>> processorList = (List<List<String>>) tmp.get(domain);
				if (processorList == null) {
					processorList = new LinkedList<List<String>>();
					tmp.put(domain, processorList);
				}

				List<List<String>> newList = (List<List<String>>) data.get("processors");

				for (List<String> list : newList) {
					processorList.add(list);
				}
			} catch (Exception ex) {
				System.err.println(file + " : Could not be processed");
				ex.printStackTrace(System.err);
			}

		}

		this.processors = tmp;

	}

}
