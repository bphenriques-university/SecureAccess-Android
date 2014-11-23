package sirs.ist.pt.secureaccess.security;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

//entirelly made to SIRS
public class DiffieHellman {

    public BigInteger p_prime = null;
    public BigInteger x_secret = null;
    public BigInteger y_device = null;
    public BigInteger g_base = null;


    public static void main(String[] args) throws Exception {

    }

    public void createKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();


        this.x_secret = ((DHPrivateKey) kp.getPrivate()).getX();
        this.y_device = ((DHPublicKey) kp.getPublic()).getY();

        final DHParameterSpec params = ((DHPublicKey) kp.getPublic()).getParams();
        this.p_prime = params.getP();
        this.g_base = params.getG();

    }

    public String generateSessionKey(BigInteger y, BigInteger p, BigInteger x) {
        BigInteger key = y.modPow(x, p);

        String result = Digest.md5(key.toString());
        return result;
    }
}