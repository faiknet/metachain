import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
	public PrivateKey privateKey; //this will be used to sign transactions
	public PublicKey publicKey; //this will act as a wallet's address
	//wallet's personal cache of unspent outputs
	public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	
	//constructor
	public Wallet() {
		generateKeyPair();
	}
	
	//this method uses Java.security.KeyPairGenerator to generate an Elliptic Curve KeyPair
	//which makes and sets our public and private keys
	public void generateKeyPair() {
		try {
			//fetch ECDSA key gen from BouncyCastle
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			//SHA1PRNG will generate a cryptographically secure number
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			//define elliptic curve
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			//initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			//set the public and private keys from the keyPair
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	//returns balance and stores the UTXOs owned by this wallet in this.UTXOs
	public float getBalance() {
		float total = 0;
		for(Map.Entry<String, TransactionOutput> item : MetaChain.UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			if(UTXO.isMine(publicKey)) { //if output (coins) belong to me
				UTXOs.put(UTXO.id, UTXO); //add it to our list of unspent transactions
				total += UTXO.value;
			}
		}
		return total;
	}
	
	//generate and return a new transaction from this wallet
	public Transaction sendFunds(PublicKey _recipient, float value) {
		if(getBalance() < value) { //gather balance and check funds
			System.out.println("#Not enough funds to send transaction. Transaction discarded.");
			return null;
		}
		//create array list of inputs
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		float total = 0;
		for(Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
			TransactionOutput UTXO = item.getValue();
			
			total += UTXO.value;
			
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		//create transaction with this wallet as sender
		Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
		newTransaction.generateSignature(privateKey); //sign it
			
		//remove spent UTXOs from local cache
		for(TransactionInput input : inputs) {
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}
}
