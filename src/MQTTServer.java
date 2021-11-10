import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;

public class MQTTServer implements Runnable {

	private Socket socket;
	private static ArrayList<Subscription> topics = new ArrayList<Subscription>();
	private static ArrayList<RetainedMessage> messages = new ArrayList<RetainedMessage>();

	public MQTTServer(Socket socket, ArrayList<Subscription> topics) { // this is used when establishing connections
		this.socket = socket;
		this.topics = topics;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int port = 1884; // default port
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Listening on port: " + port+"\n"); // port is either 8080 or passed by argument

		while (true) { // continuous connections
			Socket socket = serverSocket.accept(); // establish connection
			Thread newConnect = new Thread(new MQTTServer(socket, topics));
			newConnect.start(); // make new thread and start it, pass the socket and docRoot

		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Socket socket = this.socket;
		ArrayList<Subscription> topics = this.topics;

		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
			DataInputStream din = new DataInputStream(socket.getInputStream());

			int type = din.read();
			while (type != -1) {
				//int type = din.read(); //message type
				if ((type >> 4) == 1) { // if type is connect
					System.out.println("Header Flag: " + Integer.toHexString(type)
					+ ", so calling MQTTConnect(). Coming from " + socket.getPort());
					MQTTConnect(in, dout, type, socket);
				}

				type = din.read();
				if ((type >> 4) == 3) { // publish
					System.out.println("Header Flag: " + Integer.toHexString(type)
					+ ", so calling MQTTPublish(). Coming from " + socket.getPort());
					MQTTPublish(din, dout, type, socket);
				}
			
				if ((type >> 4) == 8) { // sub
					System.out.println("Header Flag: " + Integer.toHexString(type)
					+ ", so calling MQTTSubscribe(). Coming from " + socket.getPort());
					MQTTSubscribe(din, dout, type, socket, false); //boolean is false bc its sub request
				}
				
				if ((type >> 4) == 10) { // unsub
					System.out.println("Header Flag: " + Integer.toHexString(type)
					+ ", so calling MQTT[un]Subscribe(). Coming from " + socket.getPort());
					MQTTSubscribe(din, dout, type, socket, true); //true in param for unsubscribe
				}

				if (type >> 4 == 12) { // ping req
					System.out.println("Header Flag: " + Integer.toHexString(type)
					+ ", so calling MQTTPing(). Coming from " + socket.getPort());
					MQTTPing(din, dout, type, socket);
				}
				
				if(type >> 4 == 14) { //disconnect\
					System.out.println("Header Flag: " + Integer.toHexString(type)
					+ ", so closing socket. Coming from " + socket.getPort());
					socket.close();
					
				}	
				System.out.println("*** SUBSCRIPTIONS *** ");
				for(int i = 0; i < topics.size(); i++) {
					System.out.println("Index: "+i+" topicName = "+topics.get(i).topicName+" Port: "+topics.get(i).socket.getPort());	
				}
				System.out.println();
			}

		} catch (IOException e) {
			//e.printStackTrace();
		}

	}


	private void MQTTSubscribe(DataInputStream din, DataOutputStream dout, int type, Socket socket, boolean unsub) throws IOException {
		int msgLen = din.read();
		int msgMSB = din.read();
		int msgLSB = din.read();
		int msgID = combineBytes(msgMSB, msgLSB);
		byte topicLenMSB = din.readByte();
		byte  topicLenLSB = din.readByte();
		int topicLength = combineBytes(topicLenMSB, topicLenLSB);
		StringBuilder topicName = new StringBuilder("");
		for (int i = 0; i < topicLength; i++) {
			topicName.append((char) din.read()); // for every byte remaining (which is just the clientname) cast it to a
			// char so 6d = m and add it to string builder
		}
		
		if(unsub) {
			System.out.println("Searching for Subscriptions with topicName: "+topicName+" in from "+socket.getPort()+" to remove.");
			for(int i = 0; i < topics.size(); i++) {
				if(topics.get(i).topicName.equals(topicName.toString()) && topics.get(i).socket.getPort() == socket.getPort()) {
					System.out.println("Removing index: "+i+" topicName: "+topics.get(i).topicName+" Port: "+topics.get(i).socket.getPort());
					topics.remove(i); //remove every subscription that socket may have to that topicName
				}
			
			}
			
		}else {
			int qos = din.read(); //last byte in payload
			topics.add(new Subscription(topicName.toString(), socket));
			System.out.println("Port " + socket.getPort() + " sent SUBSCRIBE to topic '" + topicName.toString()
			+ ".' Adding to ArrayList. Sending back SUBACK.");
			MQTTSuback(dout, msgMSB, msgLSB, qos);
			System.out.println("New Subscription added, flushing out possible retained messages");
			for(int i = 0; i < messages.size(); i++) {
				if((new RetainedMessage(null, topicName.toString()).equals(messages.get(i)))){ 
					dout.write(messages.get(i).messageBuf);
				}
			}
			
		}
		
	}

	private void MQTTSuback(DataOutputStream dout, int msgMSB, int msgLSB, int qos) throws IOException {
		byte[] ack = new byte[5];
		ack[0] = (byte) 0x90;
		ack[1] = 0x03;
		ack[2] = (byte) msgMSB;
		ack[3] = (byte) msgLSB;
		ack[4] = (byte) qos; // 0x00 is connection accepted, 0x10 is connection refused, etc.
		dout.write(ack);

	}

	private void MQTTPublish(DataInputStream din, DataOutputStream dout, int type, Socket socket) throws IOException {
		int mask = 0x3; // 0000 0011
		int qos = mask & (type >> 1); // (0000 0011) & (0011 0xx0 -> 0001 10xx) //gets all 0 except the last two bits
		mask = 0x1; //0000 0001
		int retain = mask & type;
		System.out.println("Retain: "+retain);
		int msgLen = din.read();
		byte[] frameBuf = new byte[msgLen + 2]; 
		
		frameBuf[0] = (byte) type;
		frameBuf[1] = (byte) msgLen;
		for (int i = 0; i < msgLen; i++) {
			frameBuf[i + 2] = din.readByte(); //fill buffer with whole publish request
		}

		int topicLength = combineBytes(frameBuf[2], frameBuf[3]);

		StringBuilder topicName = new StringBuilder("");
		for (int i = 0; i < topicLength; i++) {
			topicName.append((char) frameBuf[4 + i]);  //topicName starts at byte 5, index 4
		}

		StringBuilder message = new StringBuilder("");
		if(qos == 0) { //no msg id
			for (int i = 4 + topicLength; i < msgLen + 2; i++) { //first 4 bytes are type, msgLen(2 bytes), topicLength, THEN we skip the whole actual topicName (denoted with  + topicLength) and then +2 for msgID 
				message.append((char) frameBuf[i]); // for every byte remaining, will  be msg
			}
		}else { //msg id, account for  the extra 2 bytes beforee parsing  the actual message	
			for (int i = 4 + topicLength + 2; i < msgLen + 2; i++) { //first 4 bytes are type, msgLen(2 bytes), topicLength, THEN we skip the whole actual topicName (denoted with  + topicLength) and then +2 for msgID 
				message.append((char) frameBuf[i]); // for every byte remaining, will  be msg
			}
			
		}
		
		if(retain == 1) {
			System.out.print("Retain flag is set, adding "+message+" to "+topicName+" to ArrayList");
			messages.add(new RetainedMessage(frameBuf, topicName.toString()));
		}
		
		System.out.println("Sending '" + message.toString() + "' to " + topicName.toString() + " (if it exists).");
		PublishMessage(topicName.toString(), frameBuf); //send msg to subs
		
		if (qos == 1) { 			// puback (has msgID)
			dout.write((byte) 0x40);
			dout.write((byte) 0x02);
			dout.write(frameBuf[4+topicLength]); //msgID MSB
			dout.write(frameBuf[4+topicLength+1]); //msgID LSB
		}
		
//		if(qos == 2) { //pubrec
//			System.out.println("QOS = 2, sending PUBREC");
//			dout.write((byte) 0x50);
//			dout.write((byte) 0x02);
//			dout.write(frameBuf[4+topicLength]); //msgID MSB
//			dout.write(frameBuf[4+topicLength+1]); //msgID LSB
//			if(din.read() >> 4 == 6) {
//				System.out.println("GOT A PUBREL. SENDING PUBCOMP");
//				dout.write((byte) 0x70);
//				dout.write((byte) 0x02);
//				dout.write(frameBuf[4+topicLength]); //msgID MSB
//				dout.write(frameBuf[4+topicLength+1]); //msgID LSB
//			}
//		}
		
	}

	private void PublishMessage(String topicName, byte[] messageBuf) throws IOException {
		if (topicName.toString().charAt(0) == '$') {
			System.out.println("Topic cannot begin with $. Exiting publish..");
			return;
		}

		for (int i = 0; i < topics.size(); i++) {
			if (topics.get(i).equals(new Subscription(topicName.toString(), socket))) {
				System.out.println("Found a subscription to topic '" + topicName.toString() + ",' coming from port "
						+ topics.get(i).socket.getPort() + ". Forwarding (publishing) msg.");
				DataOutputStream dout = new DataOutputStream(topics.get(i).socket.getOutputStream());
				Thread newPublish = new Thread(new PublishMessage(messageBuf, dout));
				newPublish.start();

			}
		}

	}

	private void MQTTConnect(BufferedReader in, DataOutputStream dout, int type, Socket socket) throws IOException { // parse connect  and send ack
		int msgLen = in.read(); // remaining length is second byte, rest of frame has this many bytes
		byte returnCode;
		byte[] frameBuf = new byte[msgLen];
		for (int i = 0; i < msgLen; i++) {
			frameBuf[i] = (byte) in.read();
		}

		int clientIDLength = combineBytes(frameBuf[10], frameBuf[11]); // most significant byte first

		StringBuilder clientName = new StringBuilder("");
		for (int i = 12; i < msgLen; i++) {
			clientName.append((char) frameBuf[i]); // for every byte remaining (which is just the clientname) cast it to a char so 6d = m and add it to string builder
		}

		System.out.println("Client " + clientName.toString() + " sent CONNECT. Sending back CONNACK.");
		returnCode = 0x00;
		MQTTConnack(dout, returnCode);
	}

	private void MQTTConnack(DataOutputStream dout, byte returnCode) throws IOException {
		byte[] ack = new byte[4];
		ack[0] = 0x20;
		ack[1] = 0x02;
		ack[2] = 0x00;
		ack[3] = returnCode; // 0x00 is connection accepted, 0x10 is connection refused, etc.

		dout.write(ack);
	}
	

	private void MQTTPing(DataInputStream din, DataOutputStream dout, int type, Socket socket) throws IOException {
		dout.write((byte) 0xd0); // ping resp
		dout.write((byte) 0x00); // msglen

	}
	
	private int combineBytes(int msgMSB, int msgLSB) { // most sig byte and least sig byte
		return (((msgMSB & 0xFF) << 8) | (msgLSB & 0xFF));
	}


}
