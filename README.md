smtp2json
=========

Inbound SMTP Server - Forwards parsed JSON over HTTP


smtp2json is a standalone application that listens at port 25 for emails as a standard SMTP Server. Parses the incoming emails and forwards them to registered url as a post message. Following features are implemented

- TLS Support
- Email parsing to extract txt/plain, txt/html and binary attachments


#### Installation
#### Using Jar file
Download binary : smtp2json-1.0-jar-with-dependencies.jar
Create config.properties in the same folder as above jar. See config.properties for instructions.
    java -jar smtp2json-1.0-jar-with-dependencies.jar

#### Using Sources
Download source or clone the Github repository. And execute
    mvn package, you will get binary