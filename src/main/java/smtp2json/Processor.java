package smtp2json;

import java.util.List;

/**
 * Actual processor for the mail. There can be more than one processor for given
 * domain. see {@link ProcessorDispatcher}
 * 
 * @author Anil Pathak
 * 
 */
public interface Processor {
	/**
	 * Returns the name of the processor. This name can be used in the
	 * configuration files.
	 * 
	 * @return Name of the processor.
	 */
	public String getName();

	/**
	 * Processes the email.
	 * 
	 * @param chainEnd
	 *            Result from the last Processor executed in the chain. The
	 *            order is defined by the order they are defined in the
	 *            configuration file. First in the chain receives the
	 *            InputStream for the mime message.
	 * @param domain
	 *            domain name
	 * @param from
	 *            from address
	 * @param to
	 *            to address
	 * @param id
	 *            id of the message generated, same between the processors.
	 * @param args
	 *            Arguments passed from the config file, first item is the name
	 *            of the processor.
	 * @return Result of the object. If stream is the result, it will be closed
	 *         by either dispatcher or next processor in the chain.
	 * @throws Exception
	 *             Any exception, will halt the flow of the chain.
	 */
	public Object process(Object chainEnd, String domain, String from, String to, String id, List<String> args) throws Exception;
}
