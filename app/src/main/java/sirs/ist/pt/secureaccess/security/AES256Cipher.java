package sirs.ist.pt.secureaccess.security;


import org.keyczar.Crypter;

public class AES256Cipher {

    public static void main(String[] args) throws Exception {
        KeyczarReaderString key_string = new KeyczarReaderString("DummyPassowrd");

        try{
            Crypter crypter = new Crypter(key_string);
            String ciphertext = crypter.encrypt("Secret message");
            System.out.println(ciphertext);

        }catch(Exception e){
            //ignore...
        }
    }


}