import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.spec.*;
import java.security.spec.*;
import java.security.*;

public class CipherText {

	public static String encrypt(String text, String key) throws Exception {
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
         	Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        	cipher.init(Cipher.ENCRYPT_MODE, aesKey);
	        byte[] encrypted = cipher.doFinal(text.getBytes());
		byte[] encryptedMsg = Base64.encodeBase64(encrypted);
		return new String(encryptedMsg);
	}

	public static String decrypt(String text, String key) throws Exception {
		byte[] enc = Base64.decodeBase64(text);
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, aesKey);
		String decrypted = new String(cipher.doFinal(enc));

		return decrypted;
	}
	
	public static void main(String [] args) throws Exception {
		String text = "Hello World";
		String encrypted = "AYZs7rTETkRslbusr1U/4A==";
		String key = "1234567891234567";

		System.out.println(decrypt(encrypt(text, key),key));
		//System.out.println(decrypt(encrypted, key));
	}
}
