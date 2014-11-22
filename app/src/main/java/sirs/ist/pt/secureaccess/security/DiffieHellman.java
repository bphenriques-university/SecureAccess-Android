package sirs.ist.pt.secureaccess.security;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

public class DiffieHellman {

    public final static int pValue = 47;

    public final static int gValue = 71;

    public final static int XaValue = 9;

    public final static int XbValue = 14;

    public static void main(String[] args) throws Exception {
        /*BigInteger p = new BigInteger(Integer.toString(pValue));
        BigInteger g = new BigInteger(Integer.toString(gValue));
        BigInteger Xa = new BigInteger(Integer.toString(XaValue));
        BigInteger Xb = new BigInteger(Integer.toString(XbValue));

        createKey();

        int bitLength = 512; // 512 bits
        SecureRandom rnd = new SecureRandom();
        p = BigInteger.probablePrime(bitLength, rnd);
        g = BigInteger.probablePrime(bitLength, rnd);

        createSpecificKey(p, g);*/

        createKeys();
    }


    public static void createKeys() throws Exception{
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();

        BigInteger x = ((javax.crypto.interfaces.DHPrivateKey) kp.getPrivate()).getX();
        BigInteger y = ((javax.crypto.interfaces.DHPublicKey) kp.getPublic()).getY();

        DHParameterSpec params = ((javax.crypto.interfaces.DHPublicKey) kp.getPublic()).getParams();
        BigInteger p = params.getP();
        BigInteger g = params.getG();

        System.out.println("Prime: " + p.toString());
        System.out.println("Base: " + g.toString());
        System.out.println("Secret value: " + x.toString());
        System.out.println("Y: " + y.toString());


        //SUCCESS IT IT IS CORRECT
        System.out.println(g.modPow(x,p));

        byte[] xBytes = x.toByteArray();
        byte[] yBytes = y.toByteArray();
        byte[] pBytes = p.toByteArray();
        byte[] gBytes = g.toByteArray();
    }

    //generates p, q, kA
    public static void createKey() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");

        DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp.getPublic(), DHPublicKeySpec.class);
        //the secret value
        System.out.println(kp.getPublic().toString());


        BigInteger x = ((javax.crypto.interfaces.DHPrivateKey) kp.getPrivate()).getX();
        System.out.println(x.toString());

        //the base b
        System.out.println(kspec.getG().toString());

        //the prime p
        System.out.println(kspec.getP().toString());

        //the public value y
        System.out.println(kspec.getY().toString());
    }

    //generates yB given p and g
    public static void createSpecificKey(BigInteger p, BigInteger g) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

        DHParameterSpec param = new DHParameterSpec(p, g);
        kpg.initialize(param);
        KeyPair kp = kpg.generateKeyPair();

        KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");

        DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp.getPublic(), DHPublicKeySpec.class);
    }




}
