package sirs.ist.pt.secureaccess.security;

import org.keyczar.exceptions.KeyczarException;
import org.keyczar.interfaces.KeyczarReader;

public class KeyczarReaderString implements KeyczarReader {

    private String key = null;
    
    public KeyczarReaderString(String key){
        this.key = key;
    }

    @Override
    public String getKey(int i) throws KeyczarException {
        return this.key;
    }

    @Override
    public String getKey() throws KeyczarException {
        return this.key;
    }

    @Override
    public String getMetadata() throws KeyczarException {
        return "Metadata??? lol";
    }
}
