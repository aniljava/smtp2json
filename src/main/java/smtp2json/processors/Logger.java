package smtp2json.processors;

import java.util.List;

import smtp2json.Processor;

public class Logger implements Processor {

	public static final String	NAME	= "logger";

	@Override
	public Object process(Object chainEnd, String domain, String from, String to, String id, List<String> args) throws Exception {
		if (args.size() > 1) {
			if ("enabled".equals(args.get(1))) {
				System.out.println("Reception Log" + "\t" + System.currentTimeMillis() + "\t" + domain + "\t" + from + "\t" + to + "\t" + id);
			}
		}
		return chainEnd;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
