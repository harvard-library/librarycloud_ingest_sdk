package edu.harvard.librarycloud.sdk;

import java.io.IOException;

import org.apache.log4j.Logger;
// import org.json.JSONObject;
// import org.json.JSONTokener;
// import org.json.XML;

import edu.harvard.librarycloud.sdk.LibCommMessage;
import edu.harvard.librarycloud.sdk.LibCommMessage.Payload;

public class ExampleProcessor implements IProcessor {
	protected Logger log = Logger.getLogger(ExampleProcessor.class); 	
	
	public void processMessage(LibCommMessage libCommMessage) throws Exception {	
	
		log.debug("Processing a message");
        
	}
}
