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
	{
    "text":"TEST BODY",
    "html":"<font><font face=\"trebuchet ms,sans-serif\">TEST BODY</font></font>",
    "From":"Example Last Name <example@exampledomain.com>",
    "To":"example Last Name <example@exampledomain.com>",    
    "Subject":"TEST EMAIL WITH ATTACHMENT",
    "attachments":[{
        "data":"/9j/4AAQSkZJRgABAQEAYA ..REST OF THE CONTENT IN BASE 64 .. UUUUUV//9k=",
        "filename":"image002.jpg",
        "Content-Type":"image/jpeg"
    }],

    "MIME-Version":"1.0",
    "Message-ID":"<long-gmail-internal-id@mail.gmail.com>",
    "partsCount":2,
    "body0":{
        "Content-Transfer-Encoding":"base64",
        "Content-Disposition":"attachment; filename=\"image002.jpg\"",
        "X-Attachment-Id":"f_h3sdw3ux0",
        "Content-Type":"image/jpeg; name=\"image002.jpg\""
    },
    "Delivered-To":"example@exampledomain.com",
    "Date":"Sat, 23 Jun 2012 02:44:25 -0500",
    "Received":["by 10.205.114.141 with HTTP; Sat, 23 Jun 2012 00:44:25 -0700 (PDT)"],
    "Content-Type":"multipart/mixed; boundary=bcaec517c662fe165004c31eea3f",
    }
    
Following fields are added
- text (if text/plain part exists or if text/plain does not exist and text/<unknown> exists)
- html (if text/html part exists)
- attachments

Headers, To, From, Subject are copied as they are. Nested body's headers are copied in nexted body and named body0, body1 ...


