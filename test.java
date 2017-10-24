import java.util.BitSet;

public class test {
    public static void main(String args[]) {
        boolean[] booleans = new boolean[]{false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true};
        byte[] bytes = encodeToByteArray(booleans);
        for (byte by : bytes) {
            System.out.println(String.valueOf(by));
        }
        boolean[] res = convert(bytes);
        for (boolean bl : res){
            System.out.println(String.valueOf(bl));
        }
    }

    private static byte[] encodeToByteArray(boolean[] bits) {
    BitSet bitSet = new BitSet(bits.length);
    for (int index = 0; index < bits.length; index++) {
        bitSet.set(index, bits[index] == true);
    }

    return bitSet.toByteArray();
}

    private static boolean[] convert(byte[] bytes) {
         boolean[] result = new boolean[Byte.SIZE * bytes.length];
         int offset = 0;
         for (byte b : bytes) {
          for (int i=0; i<Byte.SIZE; i++) result[i+offset] = (b >> i & 0x1) != 0x0;
          offset+=Byte.SIZE;
         }
         return result;
    }
}
