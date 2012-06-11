package smtp2json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * Handles individual Messages. Parses and calls web application.
 * 
 * @author Anil
 * 
 */
public class Mailet implements MessageHandler {

	private String	url;
	private String	from;
	private String	to;

	public Mailet(String url) {
		this.url = url;
	}

	@Override
	public void from(String from) throws RejectException {
		this.from = from;
	}

	@Override
	public void recipient(String to) throws RejectException {
		this.to = to;
	}
	
	MimeToJson jsonParser;
	
	@Override
	public void data(InputStream in) throws RejectException, TooMuchDataException, IOException {		
		jsonParser = new MimeToJson(in);		
	}

	DefaultHttpClient			httpclient	= new DefaultHttpClient();
	final static ObjectMapper	mapper		= new ObjectMapper();

	@Override
	public void done() {
		try {
			if (url == null)
				return;
			if (from == null || to == null)
				return; // Do nothing
			
			if(jsonParser == null){
				return;
			}

			String json = jsonParser.getJson();
			HttpPost post = new HttpPost(url);

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("data", json));

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Charset.defaultCharset().name());
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
			post.setEntity(entity);

			httpclient.execute(post);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
