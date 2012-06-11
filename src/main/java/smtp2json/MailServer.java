package smtp2json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

public class MailServer implements MessageHandlerFactory {

	public static void main(String[] args) throws Exception {				
		new MailServer().start();
	}

	private Properties	configuration;
	private String		dataFolder;
	private String		url;
	
	public void start() throws Exception {
		readConfiguration();
		this.dataFolder = getConfiguration("data_folder", "data/");
		new File(dataFolder).mkdirs();
		this.url = getConfiguration("url", null);		
		
		
		SMTPServer server = null;
		
		final String ssl_enabled = configuration.getProperty("ssl_enabled");
		if (ssl_enabled != null && "true".equals(ssl_enabled)) {
			server = getSSLServer();
		}else{
			server = new SMTPServer(this){
				@Override
				public String getName(){
					return getConfiguration("name", "SMTP2JSON");
				}
			};
		}
		
		
		String hostName = getConfiguration("host_name", getMachineName());
		String bindAddress = getConfiguration("bind_address", null);
		
		server.setHostName(hostName);
		if(bindAddress != null){
			server.setBindAddress(InetAddress.getByName(bindAddress));
		}//Else all interfaces.
		
		
		server.setPort(25);
		server.setRequireTLS(false);
		server.start();
		
		final int max_size = Integer.parseInt(getConfiguration("max_message_length", (1024 * 1024) + ""));		
		server.setMaxMessageSize(max_size);
	}

	private SMTPServer getSSLServer() throws Exception {
		final String keystore_file = getConfiguration("keystore", "keystore.keystore");
		final char[] keystore_password = getConfiguration("keystore_password", "changeit").toCharArray();
		final InputStream in = new FileInputStream(keystore_file);
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(in, keystore_password);

		final KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
		factory.init(keystore, keystore_password);

		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(factory.getKeyManagers(), null, null);

		final SMTPServer smtpServer = new SMTPServer(this) {
			@Override
			public String getName(){
				return getConfiguration("name", "SMTP2JSON/TLS");
			}
			
			@Override
			public SSLSocket createSSLSocket(Socket socket) throws IOException {
				final InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

				final SSLSocketFactory sf = sslContext.getSocketFactory();
				final SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));

				s.setUseClientMode(false);

				s.setEnabledProtocols(intersection(s.getSupportedProtocols(), ENABLED_PROTOCOLS));
				s.setEnabledCipherSuites(intersection(s.getSupportedCipherSuites(), ENABLED_CIPHER_SUITES));

				return s;
			}
		};

		return smtpServer;

	}

	private String getConfiguration(String name, String ifnull) {
		String value = configuration.getProperty(name);
		return (value == null || "".equals(value)) ? ifnull : value;
	}

	private void readConfiguration() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream("config.properties"));
		this.configuration = properties;
	}
	
	@Override
	public MessageHandler create(MessageContext ctx) {
		return new Mailet(url);		
	}
	
    public static final String[] ENABLED_PROTOCOLS = new String[] {
        "SSLv3",
        "TLSv1",
        "TLSv1.1",
        "SSLv2Hello",
    };
    public static final String[] ENABLED_CIPHER_SUITES = new String[] {
        "TLS_RSA_WITH_DES_CBC_SHA",
        "TLS_DHE_DSS_WITH_DES_CBC_SHA",
        "TLS_DHE_RSA_WITH_DES_CBC_SHA",
        "TLS_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA",
        "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_RSA_WITH_RC4_128_SHA",
        "TLS_RSA_WITH_RC4_128_MD5",
        "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
        "TLS_DHE_DSS_WITH_RC4_128_SHA",
        "TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA", 
        "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
        "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA",
        "TLS_RSA_WITH_AES_256_CBC_SHA",
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
        "SSL_RSA_WITH_RC4_128_MD5",
        "SSL_RSA_WITH_RC4_128_SHA",           
	};
	
	public static String[] intersection(String[] stringSetA, String[] stringSetB) {
	    Set<String> intersection = new HashSet<String>(Arrays.asList(stringSetA));
	    intersection.retainAll(Arrays.asList(stringSetB));
	    return intersection.toArray(new String[intersection.size()]);
	}
	
	private static String getMachineName() throws SocketException {
		String name = null;
		Enumeration<NetworkInterface> enet = NetworkInterface.getNetworkInterfaces();

		while (enet.hasMoreElements() && (name == null)) {
			NetworkInterface net = enet.nextElement();

			if (net.isLoopback())
				continue;

			Enumeration<InetAddress> eaddr = net.getInetAddresses();

			while (eaddr.hasMoreElements()) {
				InetAddress inet = eaddr.nextElement();

				if (inet.getCanonicalHostName().equalsIgnoreCase(inet.getHostAddress()) == false) {
					name = inet.getCanonicalHostName();
					break;
				}
			}
		}

		return name;
	}	

}
