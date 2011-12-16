package se.dahlman.karotz.wsclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

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

/**
* Ears
* 
* http://api.karotz.com/api/karotz/ears
* 
* move
* 
* Move Karotz Ears to a position or continuously
* 
* left (optional): position of left ear, must be an integer
* right (optional): position of right ear, must be an integer
* relative (optional): move is relative to current position, must be true or false
* reset (optional): reset position, must be true or false
* interactiveid (required): current session interactiveid, see Authentication and InteractiveMode section
**/

public class Ears {
	private static final Logger logger = LoggerFactory.getLogger(Ears.class);
	private static final String QUERY_BASE_URL = "http://api.karotz.com/api/karotz/";
	private static final String SERVICE="ears";

	
	public boolean rotate(int right, int left, boolean relative, String interactiveId) throws JDOMException, IOException{
		logger.info("Rotate ears");
		MultivaluedMap<String,String> paramMap = new MultivaluedMapImpl();
		paramMap.add("left", Integer.toString(left));
		paramMap.add("right", Integer.toString(right));
		paramMap.add("relative", Boolean.toString(relative));
		paramMap.add("interactiveid", interactiveId);

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(getBaseURI());

		InputStream reply = service.path(SERVICE).queryParams(paramMap).accept(
				MediaType.TEXT_XML).get(InputStream.class);
		
		System.out.println();
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document document = builder.build(reply);
		
		XMLOutputter printer = new XMLOutputter();
		printer.output(document,System.out);
		return true;
		
	}
	
//	public boolean reset(String interactiveId){
//		WebResource service = client.resource(getBaseURI());
//		// Get XML
////		String reply = service.path("start").queryParams(paramMap).accept(
////				MediaType.TEXT_XML).get(String.class);
//		InputStream reply = service.path("start").queryParams(paramMap).accept(
//				MediaType.TEXT_XML).get(InputStream.class);
//		
//		System.out.println();
//		SAXBuilder builder = new SAXBuilder();
//		org.jdom.Document document = builder.build(reply);
//		
//		XMLOutputter printer = new XMLOutputter();
//		printer.output(document,System.out);
//
//	}
	
	private URI getBaseURI() {
		return UriBuilder.fromUri(
				QUERY_BASE_URL).build();
	}

}
