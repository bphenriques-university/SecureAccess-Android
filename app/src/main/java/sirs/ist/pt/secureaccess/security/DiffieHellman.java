package sirs.ist.pt.secureaccess.security;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

public class DiffieHellman {

    public BigInteger p_prime = null;
    public BigInteger x_secret = null;
    public BigInteger y_device = null;
    public BigInteger g_base = null;


    public static void main(String[] args) throws Exception {

        /**** if the server has the initiative... ***/
        //HashMap<String, BigInteger> keys = createKeys();
        //BigInteger x = keys.get(X_SECRET);
        //BigInteger y = keys.get(Y_GENERATED);
        //BigInteger p = keys.get(P_PRIME);
        //BigInteger g = keys.get(G_BASE);

        //System.out.println("X: " + x.toString());
        //System.out.println("P: " + p.toString());
        //System.out.println("G: " + g.toString());
        //System.out.println("Y: " + y.toString());

        //System.out.println("Equal to y? generated: " + g.modPow(x, p).toString());

        //after the server receives the generated Y by the device he can call
        //BigInteger sessionKey = generateSessionKey(yDevice, p, x);


        /**** if the client has the initiative... ***/
        //

        BigInteger base = new BigInteger("5421644057436475141609648488325705128047428394380474376834667300766108262613900542681289080713724597310673074119355136085795982097390670890367185141189796");
        BigInteger prime = new BigInteger("13232376895198612407547930718267435757728527029623408872245156039757713029036368719146452186041204237350521785240337048752071462798273003935646236777459223");
        BigInteger xPrivate = new BigInteger("39000777136176511193021441680961616369649293084422540064423933353083443187565881746201491859471795117409175261718387");
        BigInteger yServer = new BigInteger("8826415026102002044214522724697522389530145982176923856910291018455672453648449242676975162203338436346501186930707123666094938526611575856346397246009829");

        //gerada por mim
        //String generatedKey = generateSessionKey(yServer, prime, xPrivate);

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