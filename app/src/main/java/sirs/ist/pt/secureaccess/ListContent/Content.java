package sirs.ist.pt.secureaccess.ListContent;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Content {
    public static List<Item> ITEMS = new ArrayList<Item>();
    public static Map<String, Item> ITEM_MAP = new HashMap<String, Item>();

    public static void clean(){
        ITEMS = new ArrayList<Item>();
        ITEM_MAP = new HashMap<String, Item>();
    }

    public static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.name, item);
    }

    public static class Item {
        public String name;
        public String macAddr;
        public BluetoothDevice device;

        public Item(BluetoothDevice device){
            this.name = device.getName();
            this.macAddr = device.getAddress();
            this.device = device;
        }

        @Override
        public String toString() {
            return this.device.getName();
        }
    }
}
