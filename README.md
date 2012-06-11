smtp2json
=========

Inbound SMTP Server - Forwards parsed JSON over HTTP

### Features

- Standalone
- TLS Support
- Email parsing to extract txt/plain, txt/html and binary attachments


### Installation

   java -jar smtp2json-1.0-jar-with-dependencies.jar
 
 #from source
 
    mvn package
    
### Specification
#### JSON Specification
#### JSON Client Reference
#### Configuration
#### SSL Configuration

### TODO

- Implement Regex or domain only email filtering so that the server does not appear as a open relay
