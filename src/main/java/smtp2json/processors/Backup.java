package smtp2json.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import smtp2json.Processor;

public class Backup implements Processor {
	public static final String	NAME	= "backup";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Object process(Object chainEnd, String domain, String from, String to, String id, List<String> args) throws Exception {
		if (!(chainEnd instanceof InputStream))
			return chainEnd;

		if (args.size() < 2)
			return chainEnd;

		String backupDirectory = args.get(1);

		File file = new File(backupDirectory);

		if (!new File(backupDirectory).exists()) {
			file.mkdirs();
		}

		String fileName = file.getAbsolutePath() + File.separator + id;

		OutputStream out = new FileOutputStream(fileName);
		InputStream in = (InputStream) chainEnd;
		int i;
		while ((i = in.read()) != -1) {
			out.write(i);
		}

		in.close();
		out.close();

		return new FileInputStream(fileName);
	}
}
