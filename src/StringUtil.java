import java.security.MessageDigest; //SHA256 algorithm for generating a digital fingerprint
import java.util.ArrayList;
import java.util.Base64;
import java.security.*;

public class StringUtil {
	//applies ECDSA Signature and returns the result ( as bytes ).
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
	Signature dsa;
	byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey); //load private key into the signature engine to tell it who is signing
			byte[] strByte = input.getBytes(); //convert input string into ray bytes
			dsa.update(strByte); //lets the engine know what data should be signed
			byte[] realSig = dsa.sign(); //returns signature as a byte array
			output = realSig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	//verifies a String signature by confirming if the signature was genuinely produced by the matching 
	//private key
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey); //loads the public key and switches engine into verification mode
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	//convert key into a readable string
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	//applies SHA256 to a string and returns the result
	public static String applySha256(String input) {
		try {
			//fetch the SHA-256 implementation from Java's security library
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//applies SHA256 to our input by converting the string to raw bytes
			//then runs the SHA-256 algorithm on those bytes to return an array of bytes
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			//use StringBuffer to make the array human readable as a hexademical string
			StringBuffer hexString = new StringBuffer(); 
			//convert each of the bytes to hex
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				//add 0 to any single character results
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		
	}
	//tacks in array of transactions and returns a merkle root
		public static String getMerkleRoot(ArrayList<Transaction> transactions) {
			int count = transactions.size();
			ArrayList<String> previousTreeLayer = new ArrayList<String>();
			//collect all transaction IDs into previousTreeLayer
			for(Transaction transaction : transactions) {
				previousTreeLayer.add(transaction.transactionId);
			}
			ArrayList<String> treeLayer = previousTreeLayer;
			//loops while more than 1 hash remains and terminates when there is a single hash (the merkle root)
			while(count > 1) {
			    treeLayer = new ArrayList<String>();
			    // duplicate last element if odd number
			    if(previousTreeLayer.size() % 2 != 0) {
			        previousTreeLayer.add(previousTreeLayer.get(previousTreeLayer.size() - 1));
			    }
			    for(int i = 1; i < previousTreeLayer.size(); i += 2) {
			        treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
			    }
			    count = treeLayer.size();
			    previousTreeLayer = treeLayer;
			}
			String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
			return merkleRoot;
		}
}
