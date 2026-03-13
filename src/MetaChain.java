import java.util.ArrayList;
import com.google.gson.GsonBuilder;

public class MetaChain {
	
	public static ArrayList<Block> blockChain = new ArrayList<Block>();
	public static int difficulty = 5;
	
	public static void main(String[] args) {
		//each block is mined (finds a valid mineNum)
		blockChain.add(new Block("The first block", "0"));
		System.out.println("Attempting to mine block 1...");
		blockChain.get(0).mineBlock(difficulty);
		
		blockChain.add(new Block("The second block", blockChain.get(blockChain.size()-1).hash));
		System.out.println("Attempting to mine block 2...");
		blockChain.get(1).mineBlock(difficulty);
		
		blockChain.add(new Block("The third block", blockChain.get(blockChain.size()-1).hash));
		System.out.println("Attempting to mine block 3...");
		blockChain.get(2).mineBlock(difficulty);
		
		System.out.println("\nBlock chain is valid: " + isChainValid());
		
		//format JSON using GSON
		String blockChainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
		System.out.println("\nThe block chain");
		System.out.println(blockChainJson);

	}
	
	//validation method so that any changes to the blocks will return false
	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		
		//loop through blockchain to check hasehes
		for(int i = 1; i < blockChain.size(); i++) {
			currentBlock = blockChain.get(i);
			previousBlock = blockChain.get(i-1);
			//compare registered hash and calculated hash
			if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("Current hashes are not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("Previous hashes not equal");
				return false;
			}
			
			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("This block hasn't been mined!");
				return false;
			}
		}
		return true;
	}

}
