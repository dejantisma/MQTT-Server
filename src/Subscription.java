import java.net.Socket;

public class Subscription {
	
	public String topicName;
	public Socket socket;
	
	public Subscription(String topicName, Socket socket) {
		this.topicName = topicName;
		this.socket = socket; //ports subscrib
	}
	
	@Override
	public boolean equals(Object o){ //publish is object, this.topicName is subscription. so someone subbed to test/# and we pass test/hello to publish
		boolean equal = false;
		Subscription s = (Subscription) o;
		if(o != null && o instanceof Subscription) {
			if(this.topicName.matches("(.)*(\\/#$)") || this.topicName.equals("#") || this.topicName.equals(s.topicName)) { //wildcard test2/#
				String thisSubstring = this.topicName.substring(0, this.topicName.length()-1);
				String sSubstring = s.topicName.substring(0, this.topicName.length()-1);
				if(sSubstring.equals(thisSubstring)) {
					return true;
				}
				
			}
			
			if(this.topicName.equals("+")){ //make sure one layer
				if(s.topicName.indexOf('/') == -1) { //single layer, no / char in topic
					return true;
				}
			}
			
			if(this.topicName.indexOf('/') >= 0) {
				String[] SubTopicSplit = this.topicName.split("/");
				String[] PubTopicSplit = s.topicName.split("/");
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
