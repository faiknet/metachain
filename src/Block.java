import java.util.Date;

public class Block {
	public String hash; //holds digital signature
	public String previousHash; //previous block's hash (this chains them together)
	private String data; //block data/transaction record
	private long timeStamp; //block's creation time
	private int mineNum;
	
	//constructor
	public Block(String data, String previousHash) {
		this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}
	
	//concatenates all four fields and runs them through SHA-256
	//if any field changes, the hash changes
	public String calculateHash() {
		String calculatedHash = StringUtil.applySha256(
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(mineNum) +
				data
				); //all parameters i don't want to be tampered with
		return calculatedHash;	
	}
	
	//increments mineNUm and recalculates its hash until the start starts with a number
	//of 0s equal to the difficulty to deter tampering
	public void mineBlock (int difficulty) {
		String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0" 
		while(!hash.substring(0, difficulty).equals(target)) {
			mineNum++;
			hash = calculateHash();
		}
		System.out.println("Block mined! Hash: " + hash);
	}
}
