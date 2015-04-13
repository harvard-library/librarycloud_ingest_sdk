package edu.harvard.librarycloud.sdk;

import edu.harvard.librarycloud.sdk.LibCommMessage;

interface IProcessor {

	public void processMessage(LibCommMessage message) throws Exception;

}