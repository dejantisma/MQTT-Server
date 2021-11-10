import java.io.DataOutputStream;
import java.io.IOException;

public class PublishMessage implements Runnable {
	
	byte[] frameBuf;
	DataOutputStream dout;
	
	public PublishMessage(byte[] frameBuf, DataOutputStream dout) {
		this.frameBuf = frameBuf;
		this.dout = dout;
	}

	@Override
	public void run() {
		try {
			dout.write(frameBuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
	}
}
