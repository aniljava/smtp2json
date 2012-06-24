package smtp2json;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.iharder.Base64;
import net.iharder.Base64.OutputStream;

import org.apache.james.mime4j.field.Field;
import org.apache.james.mime4j.message.BinaryBody;
import org.apache.james.mime4j.message.Body;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Entity;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class MimeToJson {
	private Map<String, Object>	map	= new HashMap<String, Object>();

	public MimeToJson(InputStream in) throws IOException {
		map.put("attachments", new LinkedList<>());
		Message entity = new Message(in);
		process(entity, map);

	}

	public String getJson() throws JsonGenerationException, JsonMappingException, IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(map);
	}

	/**
	 * Recursive method that converts given MIME Entity to JSON
	 */
	private void process(Entity entity, Map<String, Object> map) throws IOException {

		Header headers = entity.getHeader();
		List<Field> fields = headers.getFields();
		for (Field field : fields) {
			String name = field.getName();
			String value = field.getBody();

			if ("Received".equals(name)) {
				List<String> recieved = (List<String>) map.get("Received");
				if (recieved == null) {
					recieved = new LinkedList<>();
					map.put("Received", recieved);
				}
				recieved.add(value);

			} else {
				map.put(name, value);				
			}
		}

		Body body = entity.getBody();
		if (body instanceof Multipart) {
			Multipart multi = (Multipart) body;

			List<BodyPart> parts = multi.getBodyParts();
			int totalParts = parts.size();
			map.put("partsCount", totalParts);
			int index = 0;

			for (BodyPart part : parts) {
				Map<String, Object> m = new HashMap<>();
				map.put("body" + index, m);
				process(part, m);
			}
		} else {
			if (body instanceof TextBody) {
				TextBody tb = (TextBody) body;
				String text = readAll(tb.getReader());

				String contentType = (String) map.get("Content-Type");

				if (contentType == null)
					contentType = "";

				if (contentType.indexOf("text/plain") != -1) {

					if (this.map.get("text") == null) {
						this.map.put("text", text);
					}
				} else if (contentType.indexOf("text/html") != -1) {

					if (this.map.get("html") == null) {
						this.map.put("html", text);
					}
				} else {
					// Unknown body is taken as text

					if (this.map.get("text") == null) {
						this.map.put("text", text);
					}
				}
			} else if (body instanceof BinaryBody) {
				// Attachments

				List<Map<String, String>> attachments = (List<Map<String, String>>) this.map.get("attachments");

				String attachment_info = (String) map.get("Content-Disposition");
				FieldParser parser = new FieldParser(attachment_info);
				String fileName = parser.map.get("filename");

				String fileType = (String) map.get("Content-Type");
				FieldParser ctParser = new FieldParser(fileType);

				String contentType = ctParser.list.get(0);

				BinaryBody bb = (BinaryBody) body;
				InputStream in = bb.getInputStream();
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				OutputStream out = new Base64.OutputStream(data);

				int i = 0;
				while ((i = in.read()) != -1) {
					out.write(i);
				}

				in.close();
				out.close();

				Map<String, String> attachment = new HashMap<>();
				attachment.put("filename", fileName);
				attachment.put("Content-Type", contentType);
				attachment.put("data", new String(data.toByteArray(), "utf-8"));

				attachments.add(attachment);
			}

		}
	}

	private static String readAll(Reader reader) throws IOException {
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
		r.close();
		if (buffer == null)
			return "";
		return buffer.toString();
	}
	
	public static void main(String[] args) throws Exception {
		MimeToJson mj = new MimeToJson(new FileInputStream("test-email.txt"));
		String json = mj.getJson();
		System.out.println(json);
	}

}
