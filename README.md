smtp2json
=========

Inbound SMTP Server - Forwards parsed JSON over HTTP

### Overview
smtp2json is stand alone service written in java. It listens on port 25 as a standard SMTP Server. Once there is an emai, it checks the recepient's domain with the domain list in
domains list [1] configured. If the domain exists in the list. The incoming email is converted to JSON format from MIME format. The result data is sent to the a url [2] as a post
data.

It is a simple form of <http://postmarkapp.com/inbound>

### Features

- Standalone
- TLS Support
- Email parsing to extract txt/plain, txt/html and binary attachments
- Simple domain based email filtering


### Installation
	
	java -version
	# should be 1.7+
	
	-- Create a working directory
	# cd /opt/smtp2jspn && cd /opt/smtp2json
	
	-- Download binary file with dependencies
	# wget https://github.com/downloads/aniljava/smtp2json/smtp2json-1.0-jar-with-dependencies.jar
	
	-- Download sample configuration file, and change settings (Downloaded files have documentation on each option)
	# wget https://raw.github.com/aniljava/smtp2json/master/config.properties
	
	
	-- Create an empty domains.txt file add domains you want to listen to	
	# echo "" > domains.txt
	
	-- Run server as a background job	
    # java -jar smtp2json-1.0-jar-with-dependencies.jar &
 

### Configuration
See content of config.properties at <https://raw.github.com/aniljava/smtp2json/master/config.properties>

### JSON Format
TODO
