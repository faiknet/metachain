import java.security.MessageDigest; //SHA256 algorithm for generating a digital fingerprint

public class StringUtil {
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
}
