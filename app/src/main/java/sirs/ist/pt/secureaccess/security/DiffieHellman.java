package sirs.ist.pt.secureaccess.security;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

public class DiffieHellman {
    //era tarde e nao me apeteceu ver como fazer enumerados...
    public static final String P_PRIME = "P_PRIME";
    public static final String X_SECRET = "X_SECRET";
    public static final String Y_GENERATED = "Y_GENERATED";
    public static final String G_BASE = "G_BASE";

    public static void main(String[] args) throws Exception {

        /**** if the server has the initiative... ***/
        HashMap <String, BigInteger> keys = createKeys();
        BigInteger x = keys.get(X_SECRET);
        BigInteger y = keys.get(Y_GENERATED);
        BigInteger p = keys.get(P_PRIME);
        BigInteger g = keys.get(G_BASE);

        System.out.println("X: " + x.toString());
        System.out.println("P: " + p.toString());
        System.out.println("G: " + g.toString());
        System.out.println("Y: " + y.toString());

        System.out.println("Equal to y? generated: " + g.modPow(x,p).toString());

        //after the server receives the generated Y by the device he can call
        //BigInteger sessionKey = generateSessionKey(yDevice, p, x);


        /**** if the client has the initiative... ***/
        //

    }

    public static HashMap<String, BigInteger> createKeys() throws Exception{
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();


        HashMap<String, BigInteger> result = new HashMap<String, BigInteger>();

        final BigInteger x = ((DHPrivateKey) kp.getPrivate()).getX();
        final BigInteger y = ((DHPublicKey) kp.getPublic()).getY();
        result.put(X_SECRET, x);
        result.put(Y_GENERATED, y);

        final DHParameterSpec params = ((DHPublicKey) kp.getPublic()).getParams();
        final BigInteger p = params.getP();
        result.put(P_PRIME, p);
        final BigInteger g = params.getG();
        result.put(G_BASE, g);

        return result;
    }


    public static BigInteger generateSessionKey(BigInteger y, BigInteger p, BigInteger x){
        return y.modPow(x, p);
    }

}
