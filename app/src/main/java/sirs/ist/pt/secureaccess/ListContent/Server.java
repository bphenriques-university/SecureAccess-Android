package sirs.ist.pt.secureaccess.ListContent;

/**
 * Created by brunophenriques on 29/11/14.
 */
public class Server {
    String mac = null;
    String key = null;

    public Server(String mac, String key){
        this.mac = mac;
        this.key = key;
    }

    public String getMac() {
        return mac;
    }

    public String getKey() {
        return key;
    }
}
