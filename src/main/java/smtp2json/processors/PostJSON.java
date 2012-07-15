package smtp2json.processors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

/**
 * Transforms the MIME message to JSON and posts to the given URL. <br>
 * 
 * ARGUMENTS:<br>
 * 1. URL: URL Where the JSON and RAW MIME is posted. A valid url that accepts
 * [data,id,raw] parameters on POST request. e.g. ["postjson","https://..url",
 * "true"]<br>
 * 2. INCLUDERAW : Boolean value, true if raw is to be
 * passed as a raw parameter. <br>
 * 
 * RESULT:<br>
 * Returns a copy of input stream.
 * 
 * @author Anil Pathak
 * 
 */
public class PostJSON implements Processor {

	final static ObjectMapper	mapper		= new ObjectMapper();

	public static final String	NAME		= "postjson";

	DefaultHttpClient			httpclient	= new DefaultHttpClient(new PoolingClientConnectionManager());

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Object process(Object chainEnd, String domain, String from, String to, String id, List<String> args) throws Exception {

		final String url = args.get(1);
		final boolean includeRaw = (args.size() > 1) ? Boolean.parseBoolean(args.get(2)) : false;

		InputStream in = (InputStream) chainEnd;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int i;
		while ((i = in.read()) != -1) {
			out.write(i);
		}
		out.close();
		in.close();

		final byte[] data = out.toByteArray();

		MimeToJson jsonParser = new MimeToJson(new ByteArrayInputStream(data));

		try {

			String json = jsonParser.getJson();
			HttpPost post = new HttpPost(url);

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("data", json));
			formparams.add(new BasicNameValuePair("id", id));

			if (includeRaw) {
				String raw = new String(data);
				formparams.add(new BasicNameValuePair("raw", raw));
			}

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Charset.defaultCharset().name());
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
			post.setEntity(entity);

			HttpResponse response = httpclient.execute(post);
			EntityUtils.toString(response.getEntity());

			return json;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new ByteArrayInputStream(data);
	}
}
