package edu.harvard.librarycloud.sdk;

import gov.loc.mods.v3.ModsCollection;
import edu.harvard.librarycloud.sdk.LibCommMessage;

interface IProcessor {

	public void processMessage(LibCommMessage message) throws Exception;
	public void processMessage(LibCommMessage message, ModsCollection modsCollection) throws Exception;

}