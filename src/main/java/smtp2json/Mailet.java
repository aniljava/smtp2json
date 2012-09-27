package smtp2json;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.mail.internet.InternetAddress;

import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * The Main mail server creates a seperate thread per connection. A new instance
 * of this class is created every such time.
 * 
 * The responsibility of this class is to use shared ProcessorDispatcher
 * instance to check if the email can be accepted. If yes, forward the incoming
 * message which is in form if InputStream to the ProcessorDispatcher.
 * 
 * This class creates a UUID for each incoming message and passes to
 * ProcessorDispatcher.
 * 
 * 
 * @author Anil Pathak
 * 
 */
public class Mailet implements MessageHandler {

	private String				from;
	private String				to;
	private String				domain;	
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
		this.to = to.toLowerCase();
		this.domain = getDomain(this.to);
		if (!processor.accept(this.domain)) throw new RejectException();
	}

	@Override
	public void data(InputStream in) throws RejectException, TooMuchDataException, IOException {
		try {
			String id = UUID.randomUUID().toString();
			processor.process(in, domain, from, to, id, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Could not process email");
		}

	}

	@Override
	public void done() {
		/** SINCE NOTHING IS TO BE DONE THIS METHOD IS LEFT BLANK **/
	}

	/**
	 * Return the domain part of the email address. Throws RuntimeException for
	 * malformed email addresses.
	 */
	private static String getDomain(String email) {
		try {
			email = email.toLowerCase();			
			
			InternetAddress emailAddr = new InternetAddress(email);
			String domain = emailAddr.getAddress().toString();
			domain = domain.substring(domain.indexOf("@") + 1);
			return domain;
		} catch (Exception ex) {
			throw new RuntimeException(email + "NOT VALIDATED");
		}
	}
}
