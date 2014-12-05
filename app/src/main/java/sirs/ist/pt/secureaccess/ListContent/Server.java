package sirs.ist.pt.secureaccess.ListContent;

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
