package smtp2json;

import java.util.List;

public interface Processor {
	public String getName();

	public Object process(Object chainEnd, String domain, String from, String to, String id, List<String> args) throws Exception;
}
