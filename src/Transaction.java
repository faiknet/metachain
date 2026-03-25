import java.security.*;
import java.util.ArrayList;

public class Transaction {
	public String transactionId; //this is also the hash of the transaction
	public PublicKey sender; //senders address/public key
	public PublicKey recipient; //recipients address/public key
	public float value;
	public byte[] signature; //this is to prevent anybody else from spending funds in our wallet
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>(); //unspent coins
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; //a rough count of how many transactions have been generated
	
	//constructor
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;
	}
	//signs all the data that shouldn't be tampered with
	//if generate and verify every differ, verification fails
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}
	
	//verifies that the signed data hasn't been tampered with
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	//check if a new transaction can be created
	//returns true if the transaction is valid and was successfully applied
	public boolean processTransaction() {
		//reject immediately if verification fails
		if(verifySignature() == false) {
			System.out.println("#Transaction signature failed to verify...");
			return false;
		}
		
		//gather transaction inputs
		for(TransactionInput i : inputs) {
			i.UTXO = MetaChain.UTXOs.get(i.transactionOutputId);
		}
		
		//check if transaction is valid
		if(getInputsValue() < MetaChain.minimumTransaction) {
			System.out.println("#Transaction inputs are too small: " + getInputsValue());
			return false;
		}
		
		//generate transaction outputs
		float leftOver = getInputsValue() - value; //get value of inputs then the left over change
		transactionId = calculateHash(); //generate unique id for this transaction
		outputs.add(new TransactionOutput(this.recipient, value, transactionId)); //sends coins to recipient
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); //sends change to sender
		
		//add outputs to the unspent list (global UTXOs map)
		for(TransactionOutput o : outputs) {
			MetaChain.UTXOs.put(o.id, o);
		}
		
		//remove transaction inputs from UTXO lists as spent
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if transaction can't be found skip it 
			MetaChain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	//return sum of input values
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //skip inputs that can't be found 
			total += i.UTXO.value;
		}
		return total;
	}
	
	//return sum of outputs
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
	
	//calculate the transaction hash
	private String calculateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(recipient) +
				Float.toString(value) + 
				sequence
				);
	}
}
