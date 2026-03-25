//this class will be used to reference TransactionOutputs that have not yet been spent
//the transactionOutputId will be used to find the relevant TransactionOutput, allowing miners to check your ownership
public class TransactionInput {
	public String transactionOutputId; //reference to TransactionOutputs -> transactionId
	public TransactionOutput UTXO; //contains the unspent transaction output
	//constructor
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
