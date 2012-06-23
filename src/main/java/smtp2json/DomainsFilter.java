package smtp2json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.InternetAddress;

/**
 * Simple file based filter that checks if the domain part of the email exists.
 * Used to filter the domain names accepted.
 * 
 * @author Anil Pathak
 */
public class DomainsFilter {

	public Set<String>	domains	= new HashSet<>();

	/**
	 * Checks if the domain part of the email exists on the domains set.
	 * 
	 * @param email
	 *            pre validated email address.
	 * @return true if the domain exists false otherwise.
	 */
	public boolean accept(String email) {
		String domain = getDomain(email);
		return domains.contains(domain);
	}

	/**
	 * Call this method if the domain set is to be created from the file, use
	 * domains public field to update otherwise.
	 * 
	 * @param domainsFile
	 *            File containing domain names seperated by new line.
	 * @throws IOException
	 */
	public void init(String domainsFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(domainsFile));
		String domain = null;

		while ((domain = reader.readLine().trim()) != null) {
			domains.add(domain);
		}
		reader.close();
	}

	/**
	 * Returns the domain part of the email.
	 * 
	 * @param email
	 *            needs to be validated.
	 * @return
	 */
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
