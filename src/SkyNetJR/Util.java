package SkyNetJR;

import java.util.List;

public class Util {
    public static byte[] ByteListToByteArray(List<Byte> l){
        byte[] bytes = new byte[l.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = l.get(i);
        }

        return bytes;
    }
}
