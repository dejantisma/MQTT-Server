import java.net.Socket;

public class RetainedMessage {
	
	public byte[] messageBuf;
	public String topicName;
	
	public RetainedMessage(byte[] messageBuf, String topic) {
		this.messageBuf = messageBuf;
		this.topicName = topic; 
		
	}
	
	
	public boolean equals(Object o){ //publish is object, this.topicName is retainedmessage. so someone subbed to test/# and we pass test/hello to publish
		boolean equal = false;
		RetainedMessage r = (RetainedMessage) o;
		if(o != null && o instanceof RetainedMessage) {
			if(this.topicName.matches("(.)*(\\/#$)") || this.topicName.equals("#") || this.topicName.equals(r.topicName)) { //wildcard test2/#
				String thisSubstring = this.topicName.substring(0, this.topicName.length()-1);
				String rSubstring = r.topicName.substring(0, this.topicName.length()-1);
				if(rSubstring.equals(thisSubstring)) {
					return true;
				}
				
			}
			
			if(this.topicName.equals("+")){ //make sure one layer
				if(r.topicName.indexOf('/') == -1) { //single layer, no / char in topic
					return true;
				}
			}
			
			if(this.topicName.indexOf('/') >= 0) {
				String[] SubTopicSplit = this.topicName.split("/");
				String[] PubTopicSplit = r.topicName.split("/");
				if(SubTopicSplit.length != PubTopicSplit.length) {
					return false;  //not same amount of layers
				}
				
				for(int i = 0; i < SubTopicSplit.length; i++) { //make sure the KNOWN layers are equal, so +/testlayer/+  !=  +/test2layer/+
					if(SubTopicSplit[i].equals("+")) {
						//skip
					}else {
						if(!SubTopicSplit[i].equals(PubTopicSplit[i])) {
							return false;
						}
					}
					System.out.println("i = "+i+" = "+SubTopicSplit[i]);
				}
				
				return true;
				
			}
		}
		
		return equal;
	}
	
}
