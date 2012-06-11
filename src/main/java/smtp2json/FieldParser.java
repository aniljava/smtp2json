package smtp2json;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FieldParser {

	public Map<String, String>	map	= new HashMap<>();
	public List<String>		list	= new LinkedList<>();

	@Override
	public String toString() {
		return "" + map + list;
	}

	public FieldParser(String raw) {
		char[] chars = raw.toCharArray();
		for (char c : chars) {
			newchar(c);
		}
		terminate();
	}

	boolean	inName	= true;
	String	name;
	String	value;
	boolean	quote;
	boolean	escape;

	void newchar(char c) {

		if (escape) {
			append(c);
			escape = false;
			return;
		}

		if (!quote && c == ';') {
			terminate();
			return;
		}

		if (!quote && c == '"') {
			quote = true;
			return;
		}
		
		if(quote && !escape && c == '"'){
			quote = false;
			return;
		}

		if (!quote && c == '=') {
			inName = false;
			return;
		}

		if (c == '\\') {
			escape = true;
		}

		if (c == '=') {
			if (inName) {
				inName = false;
			}
		}

		append(c);

	}

	void append(char c) {
		if (inName) {
			if (name == null)
				name = "";
			name += c;
		} else {
			if (value == null)
				value = "";
			value += c;
		}
	}

	void terminate() {
		if (name != null) {
			name = name.trim();
		}
		if (name == null) {
			return;
		}

		if (value == null) {
			list.add(name);
		} else {
			map.put(name, value);
		}

		name = null;
		value = null;
		quote = false;
		inName = true;

	}

}
