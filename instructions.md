## COS 460/540 - Computer Networks
# Project 3 & 4: MQTT Server

## Specification

For the next two projects we will be creating a lightweight, stripped down, and eventually distributed [MQTT][mqtt] [v3.1.1][mqtt-spec] compatible service. The project is broken into two parts; implementing the standalone server and then extending to a distributed system with your classmates. The first part has documented protocols to implement, the second we will design in-class and online together.

## Project 3 - Basic MQTT Server

For this part of the assignment you will implement a stripped down [MQTT][mqtt] [v3.1.1][mqtt-spec] compatible server. You will use the [MQTT v3.1.1][mqtt-spec] as the outline of your implementation. You will not have to implement the entire specification (see below).

Your server MUST be able to serve multiple clients simultaneously and without blocking or crashing. Multi-threading is the most common approach to managing this though you are free to use other methods.

You MAY use any available frameworks to help you solve the problem with the exception of pre-packaged MQTT client or server libraries. The expectation is that you write your own code for the parsing of MQTT messages, the managing of client and topic states and the messages published and subscribed to.

What do you need to implement or not from the specification?

* **CONNECT** – Client requests a connection to a Server

    You MAY implement *username*, *password*, or *will* semantics. However, Your server MUST parse the messages properly while it does not not have to honor authentication or last will and testament. That is, *any* value can be accepted for username and password.

    We are going to try and avoid implementing *clean session* and the notion of *sessions* but may find this is not possible. Plan on implementing them but leave them for last portion of your work.

* **CONNACK** – Acknowledge connection request

    Implement as documented.

* **PUBLISH** – Publish message

    You SHOULD implement *retain* the best you can, but don't worry about retaining across restarts of your server. That is, you should retain messages while your server is up and running.

    We are going to try and avoid implementing Quality of Service (QoS) level 2 but again may find it unavoidable. Plan on implementing it but leave it for the last part of your work.

* **PUBACK** – Publish acknowledgement

    Implement as documented.

* **PUBREC** – Publish received (QoS 2 publish received, part 1)

    See note about QoS level 2 above under **PUBLISH**.

* **PUBREL** – Publish release (QoS 2 publish received, part 2)

    See note about QoS level 2 above under **PUBLISH**.

* **PUBCOMP** – Publish complete (QoS 2 publish received, part 3)

    See note about QoS level 2 above under **PUBLISH**.

* **SUBSCRIBE** - Subscribe to topics

    Implement as documented.

* **SUBACK** – Subscribe acknowledgement

    Implement as documented.

* **UNSUBSCRIBE** – Unsubscribe from topics

    Implement as documented.

* **UNSUBACK** – Unsubscribe acknowledgement

    Implement as documented.

* **PINGREQ** – PING request

    Implement as documented.

* **PINGRESP** – PING response

    Implement as documented.

* **DISCONNECT** – Disconnect notification

    Implement as documented.

## Project 4 - Distributed MQTT System

For this part of the assignment you will extend your server to be part of a distributed system of servers with your classmates implementing theirs to do the same. In the end, all the servers will work together to service clients that connect to any of the endpoints (your servers).

This part is much less documented as it does not yet exist! We will be developing the protocol of how our servers communicate with each other in-class during the remainder of the semester.

## Reference Materials and Resources

There are a number of documents that will be helpful in understanding the network protocols used by MQTT.

* [MQTT FAQ](https://mqtt.org/faq)  is a great starting place for what MQTT is.
* [MQTT v3.1.1 specification][mqtt-spec] is the formal specification of the protocol
* [MQTT Slides](https://www.slideshare.net/PeterREgli/mq-telemetry-transport) are an excellent overview of MQTT and MQTT packet format (as shown in class).

To facilitate your understanding of the MQTT Protocol these links might help

* [Wireshark](https://www.wireshark.org) for capturing network traffic and looking at captures
* [MQTT Packet Capture and Analysis using Wireshark](https://www.wireshark.org) video walkthrough of wireshark capturing MQTT traffic.
* [MQTT Packet Capture and Analysis using Wireshark](https://iotbytes.wordpress.com/capturing-and-analysing-mqtt-packets/) article from above video with packet capture (for wireshark) data you can load and look at.

To help you test your implementation these might help

* [Eclipse Mosquitto](https://mosquitto.org) free implementation of MQTT with `mosquitto_pub` and `mosquitto_sub` command line clients for publishing and subscribing to topics. Also includes an MQTT server that you can use as a reference implementation.
* [Eclipse paho](https://www.eclipse.org/paho/) free MQTT client libraries for a number of languages. You can also use these to test your server.
* Eclipse also runs a publically accessible sandbox server available at `mqtt.eclipse.org`, port `1883` which you can connect to and test clients against to understand how the protocol and clients work.

## Additional Information
* The server must accept as a configurable parameter (on the command line) the port number to listen on.
* You must include the file named PLAYBOOK.md
* PLAYBOOK.md has Your name
* PLAYBOOK.md has what language you used
* PLAYBOOK.md has a brief synopsis of your experience with the assignment (1-3 paragraphs).
* PLAYBOOK.md has how to compile and execute your project.
* You must not include any executable binary files. Submit only code.
* You may include a script or batch file to compile and/or run your server. This must be documented in your PLAYBOOK.md if it is included.
* You must submit your project through [GitHub](http://github.com)

## Definitions
The IETF Best Practice Document RFC 2119 Key words for use in RFCs to Indicate Requirement Levels defines several keywords that are used in this assignment and throughout the course. Pay special attention to where they appear in the assignment.

Some keywords used in this assignment are as follows;

**MUST**: This word, or the terms **REQUIRED** or **SHALL**, mean that the definition is an absolute requirement of the specification.

**SHOULD**: This word, or the adjective **RECOMMENDED**, mean that there may exist valid reasons in particular circumstances to ignore a particular item, but the full implications must be under.

**MAY**: This word, or the adjective **OPTIONAL**, mean that an item is truly optional. One vendor may choose to include the item because a particular marketplace requires it or because the vendor feels that it enhances the product while another vendor may omit the same item.

 [mqtt]: http://mqtt.org
 [mqtt-spec]: http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html