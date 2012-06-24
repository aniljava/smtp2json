package smtp2json;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.mail.internet.InternetAddress;

import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * Handles individual Messages. Parses and calls web application.
 * 
 * @author Anil Pathak
 * 
 */
public class Mailet implements MessageHandler {

	private String				from;
	private String				to;
	private String				domain;
	private String				id = UUID.randomUUID().toString();
	private ProcessorDispatcher	processor	= null;

	public Mailet(ProcessorDispatcher filter) {
		this.processor = filter;
	}

	@Override
	public void from(String from) throws RejectException {
		this.from = from;
	}

	@Override
	public void recipient(String to) throws RejectException {
		this.domain = getDomain(to);
		if (!processor.process(domain)) {
			throw new RejectException();
		}
		this.to = to;
	}

	MimeToJson	jsonParser;

	@Override
	public void data(InputStream in) throws RejectException, TooMuchDataException, IOException {
		try {
			processor.process(in, domain, from, to, id, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Could not process email");
		}

	}

	DefaultHttpClient			httpclient	= new DefaultHttpClient();
	final static ObjectMapper	mapper		= new ObjectMapper();

	@Override
	public void done() {
		// DONE !!
	}

	private static String getDomain(String email) {
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			String domain = emailAddr.getAddress().toString();
			domain = domain.substring(domain.indexOf("@") + 1);

			return domain;
		} catch (Exception ex) {
			throw new RuntimeException(email + "NOT VALIDATED");
		}
	}
}
