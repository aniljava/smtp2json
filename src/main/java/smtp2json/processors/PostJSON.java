package smtp2json.processors;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import smtp2json.MimeToJson;
import smtp2json.Processor;

public class PostJSON implements Processor {

	final static ObjectMapper	mapper		= new ObjectMapper();

	public static final String	NAME		= "postjson";

	
	DefaultHttpClient				httpclient	= new DefaultHttpClient(new PoolingClientConnectionManager());


	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public Object process(Object chainEnd, String domain, String from, String to, String id, List<String> args) throws Exception {

		String url = args.get(1);
		InputStream in = (InputStream) chainEnd;

		MimeToJson jsonParser = new MimeToJson(in);

		try {

			String json = jsonParser.getJson();
			HttpPost post = new HttpPost(url);

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("data", json));
			formparams.add(new BasicNameValuePair("id", id));

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Charset.defaultCharset().name());
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
			post.setEntity(entity);

			HttpResponse response = httpclient.execute(post);
			EntityUtils.toString(response.getEntity());
			
			return json;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}
