package edu.harvard.librarycloud.sdk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import edu.harvard.librarycloud.sdk.LibCommMessage;
import edu.harvard.librarycloud.sdk.LibCommMessage.Payload;

public class MessageBodyS3Marshaller implements DataFormat {
 
    private AWSCredentials awsCredentials;
    private int maxData;
    private String bucket;
    private String S3_PREFIX = "https://s3.amazonaws.com/";

    public MessageBodyS3Marshaller(AWSCredentials awsCredentials, int maxData, String bucket) {
        this.awsCredentials = awsCredentials;
        this.maxData = maxData;
        this.bucket = bucket;
    }

 	/* Take a message, check to see if it's greater than maxData. If so, 
 	   Take the contents of payload.body, upload them to S3 (with an expiration time), 
 	   set payload.filepath to the location of the data, and remove he contents of the body */
    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
	
		Message message = exchange.getIn();
		InputStream messageIS = MessageUtils.readMessageBody(message);			
        LibCommMessage libCommMessage = MessageUtils.unmarshalLibCommMessage(messageIS);        
                
        if ((libCommMessage.getPayload() != null) && (libCommMessage.getPayload().getData() != null)) {
            byte[] bytes = libCommMessage.getPayload().getData().getBytes(StandardCharsets.UTF_8);
    		if (bytes.length > this.maxData) {

                /* Upload to bucket with random key. We assume the bucket exists */
                String key = UUID.randomUUID().toString();           
                AmazonS3 s3Client = new AmazonS3Client(this.awsCredentials);

                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(bytes.length);
                objectMetadata.setContentType("application/xml");
                PutObjectRequest putRequest = new PutObjectRequest(this.bucket, key, new ByteArrayInputStream(bytes), objectMetadata);
                PutObjectResult putResult = s3Client.putObject(putRequest);
                
                /* Replace filepath with key to the object in s3 */
                libCommMessage.getPayload().setFilepath(S3_PREFIX + this.bucket + "/" + key);

                /* Empty body */
                libCommMessage.getPayload().setData("");
            }            
        }

		String messageString = MessageUtils.marshalMessage(libCommMessage);
        stream.write(messageString.getBytes());
    }

    /* Take a message, check to see if the filepath is set to an S3 object AND the body is empty. If
       so, download the S3 object, clear the filepath, and set the body to the contents of the object */
    public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        LibCommMessage libCommMessage = MessageUtils.unmarshalLibCommMessage(stream);

        if ((libCommMessage.getPayload() != null) &&
            ((libCommMessage.getPayload().getData() == null) || libCommMessage.getPayload().getData().isEmpty()) &&
            ((libCommMessage.getPayload().getFilepath() != null) && !libCommMessage.getPayload().getFilepath().isEmpty())) {

            String filepath = libCommMessage.getPayload().getFilepath();
            if (filepath.startsWith(this.S3_PREFIX)) {
                filepath = filepath.replace(this.S3_PREFIX,"");
                String[] parts = filepath.split("/");
                if (parts.length == 2) {
                    String bucket = parts[0];
                    String key = parts[1];
                    AmazonS3 s3Client = new AmazonS3Client(this.awsCredentials);
                    S3Object getResult = null;
                    try {
                        getResult = s3Client.getObject(bucket, key);
                        String body = IOUtils.toString(getResult.getObjectContent(), StandardCharsets.UTF_8);
                        libCommMessage.getPayload().setData(body);
                        libCommMessage.getPayload().setFilepath("");
                    } finally {
                        if (getResult != null) {
                            getResult.close();
                        }
                    }
                }
            }
        }
        String messageString = MessageUtils.marshalMessage(libCommMessage);
        return messageString;
    } 
}
