import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class MetaChain {
	
	//an ordered list of blocks, where each block references the previous one
	public static ArrayList<Block> blockChain = new ArrayList<Block>();
	//global unspent transaction map
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list of all unspent transactions
	//determines how many leading 0s a valid block hash must have (higher = more difficult to mine)
	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f; //smallest value allowed
	//test wallets
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction; //first transaction
	
	public static void main(String[] args) {
		//set up bouncy castle as a security provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		
		//create new wallets
		walletA = new Wallet();
		walletB = new Wallet();
		//coinbase wallet distributes initial coins into circulation
		Wallet coinbase = new Wallet();
		
		//create genesis transaction, which sends 100 MetaCoin to walletA
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey); //manually sign the genesis transaction
		genesisTransaction.transactionId = "0"; //manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list
		
		//genesis block created with 0 as the previousHash as it has no parent
		System.out.println("Creating and mining Genesis Block...");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		//testing
		Block block1 = new Block(genesis.hash);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		//overdraft test
		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA is attempting to send more funds (1000) that it currently has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		//reverse transaction test
		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB is attempting to send funds (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
		addBlock(block3);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		//full chain validation
		isChainValid();
	}
	
	//validation method so that any changes to the blocks will return false
	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hasehes
		for(int i = 1; i < blockChain.size(); i++) {
			currentBlock = blockChain.get(i);
			previousBlock = blockChain.get(i-1);
			//compare registered hash and calculated hash
			if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("#Current hashes are not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("#Previous hashes not equal");
				return false;
			}
			
			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined!");
				return false;
			}
			//loop through blockchains transactions
			TransactionOutput tempOutput;
			for(int t = 0; t < currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on transaction(" + t + ") is invalid.");
					return false;
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are not equal to outputs on transaction(" + t + ")");
					return false;
				}
				
				for(TransactionInput input : currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on transaction(" + t + ") is missing.");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input transaction(" + t + ") value is invalid.");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output : currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if(currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
					System.out.println("#Transaction (" + t + ") output recipient is not who it should be.");
					return false;
				}
				if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
					System.out.println("#Transaction (" + t + ") output change is not sender.");
					return false;
				}
			}
		}
		System.out.println("\nBlockchain validated.");
		return true;
	}
	
	//mines the block and appends it to the chain
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockChain.add(newBlock);
	}

}
