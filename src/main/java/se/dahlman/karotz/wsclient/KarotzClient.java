package se.dahlman.karotz.wsclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JEditorPane;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class KarotzClient {
	private static final String API_KEY= "bd029d7a-6d7b-44e8-b5a0-94131f858343";
	private static final String	SECRET_KEY= "c12aad39-62e3-4807-a842-c93afac65e3b";
	private static final String	INSTALLID = "b7ccce9a-fec2-463a-afcf-9d3de49df991";
	
	Logger logger = LoggerFactory.getLogger(KarotzClient.class);
	
	/**
	 * Start Application from installID

		http://api.karotz.com/api/karotz/start
	
		Start the application on Karotz
	
		apikey (required): API key of the application
		once (required): a random value, should never be the same
		timestamp (required): current time stamp (epoch in secods)
		signature (required): base64, HmacSHA1 sign the sum of all arguments in alphabetical order
		installid (required): installation ID of the application
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public String start() throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException{
		
		Random random = new Random(78168315);
		int once = random.nextInt(Integer.MAX_VALUE);
		int timestamp = (int)(System.currentTimeMillis() / 1000);

		logger.info("Start");
		StringBuffer paramString = new StringBuffer();
		paramString.append("apikey=").append(API_KEY);
		paramString.append("&installid=").append(INSTALLID);
		paramString.append("&once=").append(once);
		paramString.append("&timestamp=").append(timestamp);
		String params = paramString.toString();
		System.out.println(params);
		String encodedParams = URLEncoder.encode(params, "UTF-8");
		System.out.println(encodedParams);
		String signature = encrypt(params, SECRET_KEY);
		paramString.append("&signature=").append(signature);
		return paramString.toString();
//		apikey=APIKEY&installid=INSTALLID&once=15606228041&timestamp=1322290452&signature=jUa79D7QVk1//7bcTxJFheTfVUo%3D 
	}
	
	public MultivaluedMap getStartParameters() throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException{
		MultivaluedMap<String,String> paramMap = new MultivaluedMapImpl();
		//Map<String, String> paramMap = new TreeMap<String, String>();
		Random random = new Random(78168315);
		int once = random.nextInt(Integer.MAX_VALUE);
		int timestamp = (int)(System.currentTimeMillis() / 1000);
		
		StringBuffer paramString = new StringBuffer();
		paramString.append("apikey=").append(API_KEY);
		paramString.append("&installid=").append(INSTALLID);
		paramString.append("&once=").append(once);
		paramString.append("&timestamp=").append(timestamp);
		String params = paramString.toString();
		System.out.println(params);
		String encodedParams = URLEncoder.encode(params, "UTF-8");
		System.out.println(encodedParams);
		String signature = encrypt(params, SECRET_KEY);
		
		
		paramMap.add("apikey", API_KEY);
		paramMap.add("installid", INSTALLID);
		paramMap.add("once", ""+once);
		paramMap.add("timestamp", ""+timestamp);
		paramMap.add("signature", ""+signature);
		
		return paramMap;
		
	}
	
	private String encrypt(String value, String key) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException{
		byte[] keyBytes = key.getBytes();           
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

        // Get an hmac_sha1 Mac instance and initialize with the signing key
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);

        // Compute the hmac on input data bytes
        byte[] rawHmac = mac.doFinal(value.getBytes());

        // Convert raw bytes to Hex
        System.out.println(rawHmac);
        
        byte[] hexBytes = new Base64().encode(rawHmac);

        return URLEncoder.encode(new String(hexBytes), "UTF-8"); 

	}
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, JDOMException, IOException {
		KarotzClient client = new KarotzClient();
		String urlParams = client.start();
		MultivaluedMap paramMap = client.getStartParameters();
		String interactiveId = client.getIteractiveId(paramMap);
		System.out.println(urlParams);
		Ears ears = new Ears();
		ears.rotate(10, 20, false, interactiveId);
//		ears.rotate(10, 20, true, interactiveId);
	}

	private String getIteractiveId(MultivaluedMap<String,String> paramMap) throws JDOMException, IOException {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(getBaseURI());
		// Get XML
//		String reply = service.path("start").queryParams(paramMap).accept(
//				MediaType.TEXT_XML).get(String.class);
		InputStream reply = service.path("start").queryParams(paramMap).accept(
				MediaType.TEXT_XML).get(InputStream.class);
		
		System.out.println();
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document document = builder.build(reply);
		
		XMLOutputter printer = new XMLOutputter();
		printer.output(document,System.out);
		Element root = document.getRootElement();
		
		Element interaktiveMode = root.getChild("interactiveMode");
		if(interaktiveMode != null){
			Element interactiveId = interaktiveMode.getChild("interactiveId");
			return interactiveId.getText();
		}

		Element response = root.getChild("response");
		Element responseCode = response.getChild("code");
		if(responseCode.getText().equals("NOT_CONNECTED")){
			logger.error("Karotz not connected");
			throw new RuntimeException("Karotz not connected");
		} else if(responseCode.getText().equals("ERROR") ) {
			logger.error("ERROR");
			throw new RuntimeException("Karotz error");			
		}
		String result = responseCode.getText();
		logger.debug("Starng message!!!");
		return null;
	}

	private URI getBaseURI() {
		return UriBuilder.fromUri(
				"http://api.karotz.com/api/karotz/").build();
	}

}
