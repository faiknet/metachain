import java.util.ArrayList;
import java.util.Date;

public class Block {
	public String hash; //holds digital signature
	public String previousHash; //previous block's hash (this chains them together)
	public String merkleRoot; //hash that summarizes all transaction in the block using a Merkle tree
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.
	private long timeStamp; //block's creation time
	private int mineNum;
	
	//constructor
	public Block(String previousHash) {
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
				Integer.toString(mineNum) 
				); //all parameters i don't want to be tampered with
		return calculatedHash;	
	}
	
	//increments mineNum and recalculates its hash until the start starts with a number
	//of 0s equal to the difficulty to deter tampering
	public void mineBlock (int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = new String(new char[difficulty]).replace('\0', '0'); //replace all null characters with a # of 0s according to the difficulty (3 = 000)
		while(!hash.substring(0, difficulty).equals(target)) { //loop until the hash starts with an adequate # of 0s
			mineNum++;
			hash = calculateHash();
		}
		System.out.println("Block mined! Hash: " + hash);
	}
	//add transactions to this block
	public boolean addTransaction(Transaction transaction) {
		//process transaction and check if valid unless block is genesis block then ignore
		if(transaction == null) return false; //check if transaction exists
		if((!previousHash.equals("0"))) { //genesis block bypass
			if((transaction.processTransaction() != true)) { //fail state
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		transactions.add(transaction); //success state
		System.out.println("Transaction successfully added to block.");
		return true;
	}
}
