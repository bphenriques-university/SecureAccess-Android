package sirs.ist.pt.secureaccess.security;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CipherText {

    public static String encrypt(String text, byte[] key) throws Exception {
        Key aesKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        byte[] encryptedMsg = Base64.encode(encrypted, Base64.DEFAULT);
        return new String(encryptedMsg);
    }

    public static String decrypt(String text, byte[] key) throws Exception {
        byte[] enc = Base64.decode(text, Base64.DEFAULT);
        Key aesKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted = new String(cipher.doFinal(enc));

        return decrypted;
    }

    public static void main(String [] args) throws Exception {
        String text = "Hello World";
        String encrypted = "AYZs7rTETkRslbusr1U/4A==";
        String key = "1234567891234567";

        //System.out.println(decrypt(encrypt(text, key),key));
        //System.out.println(decrypt(encrypted, key));
    }
}

