package smtp2json;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.apache.james.mime4j.message.BinaryBody;
import org.apache.james.mime4j.message.Body;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;

import org.codehaus.jackson.map.ObjectMapper;

import org.subethamail.smtp.MessageContext;
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
	
	private MessageContext	ctx;
	
	private String			id;
	private String			from;
	private String			to;
	private String			text;
	private String			html;
	private String			dataFolder;
	private String			url;
	private String			subject;
	

	public Mailet(MessageContext ctx, String dataFolder, String url) {
		this.ctx = ctx;
		this.url = url;
		this.dataFolder = dataFolder;
		this.id = UUID.randomUUID().toString();
	}

	@Override
	public void from(String from) throws RejectException {
		this.from = from;
	}

	@Override
	public void recipient(String to) throws RejectException {
		this.to = to;
	}

	@Override
	public void data(InputStream in) throws RejectException, TooMuchDataException, IOException {
		
		//Save a copy as it is.
		OutputStream out = new GZIPOutputStream(new FileOutputStream(dataFolder + id));
		int i = 0;
		while ((i = in.read()) != -1) {
			out.write(i);
		}
		in.close();
		out.close();
		
		InputStream fin = new GZIPInputStream( new FileInputStream(dataFolder + id));
		Message message = new Message(fin);
		this.subject = message.getSubject().getValue();
		
		Body body = message.getBody();
		
		processMultiPart(body, message.getMimeType());		
	}

	
	DefaultHttpClient			httpclient	= new DefaultHttpClient();
	final static ObjectMapper	mapper	= new ObjectMapper();
	@Override
	public void done() {
		if(url == null)return;		
		if(from == null || to == null)return; //Do nothing
		if(text == null && html == null)return;
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("id", id);
		map.put("timestamp", System.currentTimeMillis() + "");
		map.put("from", from);
		map.put("to", to);
		map.put("subject", subject);
		map.put("txt", text);
		map.put("html", html);
		
		InetSocketAddress address = (InetSocketAddress)ctx.getRemoteAddress();
		
		
		//Use instanceof to extract possible
		
		map.put("from_host", address.getHostName());
		map.put("from_ip", address.getAddress().toString());
		
		try{
			String json = mapper.writeValueAsString(map);			
			HttpPost post = new HttpPost(url);
			
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("data", json));
			
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Charset.defaultCharset().name());
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(entity);
			
            HttpResponse response = httpclient.execute(post);
            InputStream in = response.getEntity().getContent();
            if(in != null){
            	String result = readAll(new InputStreamReader(in));
            	System.out.print(result);
            }
            
            
            
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//Convert to 

	}
	
	
	
	private void processMultiPart(Body part, String mime) throws IOException {
		if (part instanceof Multipart) {
			Multipart multi = (Multipart) part;

			List<BodyPart> parts = multi.getBodyParts();
			for (BodyPart body : parts) {
				processMultiPart(body.getBody(), body.getMimeType());
			}
		} else {
			if ("text/plain".equals(mime) && part instanceof TextBody) {
				TextBody txt = (TextBody) part;
				this.text = readAll(txt.getReader());
				return;
			}

			if ("text/html".equals(mime) && part instanceof TextBody) {
				TextBody txt = (TextBody) part;
				this.html = readAll(txt.getReader());
				return;
			}

			// Multiple Attachments.

			if (part instanceof BinaryBody) {
				// TODO. Need to work on entire processing to extract filenames from headers.
				// Binary body gives the stream with content but not the names of the file.
			}

		}
	}
	
	private String readAll(Reader reader) throws IOException {
		StringBuffer buffer = null;

		BufferedReader r = new BufferedReader(reader);
		String line = null;

		while ((line = r.readLine()) != null) {
			if (buffer == null) {
				buffer = new StringBuffer(line);
			} else {
				buffer.append("\n");
				buffer.append(line);
			}
		}
			if(buffer == null) return "";
		return buffer.toString();
	}
	

}
