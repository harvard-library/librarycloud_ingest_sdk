# Goals

The LibraryCloud Ingest SDK makes it possible to add additional steps to the LibraryCloud pipeline.

# Prerequsites

* Software: Maven, Git, Java
* AWS account

# Installation and Configuration

* Fork this repository on Github, and clone it to your local environment
* Login to your AWS account, go to the [SQS Control Panel](https://console.aws.amazon.com/sqs/home), and create an SQS queue on which to receive inbound messages
* Set permissions on that queue such that the SendMesage policy is enabled for all users
* Copy ```src/main/resources/aws.properties.example``` to ```src/main/resources/aws.properties``` and update to include your AWS Access Key and Secret Key
* Copy ```src/main/resources/librarycloud.env.properties.example``` to ```src/main/resources/librarycloud.env.properties```
* Edit ```src/main/resources/META-INF/sprint/camel-context.xml``` and replace ```{{librarycloud.sqs.environment}}-publish-public``` with the name of your SQS queue for inbound messages.

# Execution

From the home directory of the project, run 

```mvn camel:run```

This command will build and run the Apache Camel pipeline in the foreground, printing output to the console and ```logs/librarycloud.log```. When initialization is complete you will see 

```INFO - Apache Camel 2.14.1 (CamelContext: sqsContext) started in XXX seconds``` 

printed to the log.

As part of initialization, the camel process will create any SQS queues defined in ```camel-context.xml``` that do not already exist. In the default configuration these will be ```librarycloud-developer-done``` and ```librarycloud-developer-dead-letter```.

Nothing will happen until a message appears on a queue that the process is monitoring. To validate the configuration, copy the text from ```data/sample_message_01.xml``` and manually post a new message to the inbound message SQS queue by right-clicking on the queue name and selecting "Send a Message".

When the local process picks up the message, some additional output will be printed. If the sample pipeline componenet is invoked correctly, it will print 

```====== Processing a message through the ExampleProcessor =======``` 

to the screen, and the message will be moved to the ```librarycloud-developer-done``` queue. If there was an error, it will be moved to the ```librarycloud-developer-dead-letter``` queue, and details of the error will be added to the message.

# Connecting to the pipeline

Contact the LibraryCloud Support team, and give them the ARN of the SQS queue to be used for inbound messages. Once the SQS queue is added to the SNS subscription, messages will start flowing to the specified queue.

There are three SNS subscriptions available

* Full - A full set of all LibraryCloud records (over 10 million) is published weekly. Incremental updates are published two or three times a week.
* Updates - Only LibraryCloud records that have changed or been added in the previous interval. Published several times a week.
* Deletes - A list of record ids for records that previously existed, but have been deleted in the previous interval. Published several times a week


# Writing a component to do something

The camel pipeline invokes the ```processMessage()``` functions defined in ```ExampleProcessor.java```. Change these to reflect the logic you wish to have included in your component. 

If the payload of the document contains a set of XML MODS records, ```processMessage(libCommMessage, modsCollection)``` will be called, providing  object representations of the message and the MODS records. 

If the payload does not contain MODS records, ```processMessage(libCommMessage)``` will be called, and the payload will be accessible by calling ```libCommMessage.getPayload().getData()```. 

A sample of XML MODS records that might appear in a payload is available at ```data/sample_message_01-payload.xml```.


# Scaling up and running on EC2 (WIP)

* Commit the code to Github
* Install Vagrant and the vagrant AWS plugin
* Get default box (??)
* Setup environment variables for AWS
* vagrant up
* Find out name of new server
* Create auto-scaling group
* Start auto-scaling group
* Destroy auto-scaling group
