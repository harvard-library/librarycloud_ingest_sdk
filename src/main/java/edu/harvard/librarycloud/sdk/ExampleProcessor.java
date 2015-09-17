package edu.harvard.librarycloud.sdk;

import java.io.IOException;
import org.apache.log4j.Logger;
import gov.loc.mods.v3.ModsCollection;

import edu.harvard.librarycloud.sdk.LibCommMessage;
import edu.harvard.librarycloud.sdk.LibCommMessage.Payload;

public class ExampleProcessor implements IProcessor {
	protected Logger log = Logger.getLogger(ExampleProcessor.class); 	
	
	public void processMessage(LibCommMessage libCommMessage) throws Exception {	
	
		log.debug("====== Processing a message through the ExampleProcessor =======");
     
	}

	public void processMessage(LibCommMessage libCommMessage, ModsCollection modsCollection) throws Exception {	
	
		log.debug("====== Processing a MODS message through the ExampleProcessor with " + modsCollection.getMods().size() + " items =======");
     
	}
}
