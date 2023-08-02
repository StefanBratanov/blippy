package blippy.encoding;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Bencoding {

  private static final Bencode BENCODE = new Bencode(StandardCharsets.ISO_8859_1);

  public static byte[] encodeMap(final Map<?, ?> map) {
    return BENCODE.encode(map);
  }

  public static Map<String, Object> decodeDictionary(final byte[] bytes) {
    return BENCODE.decode(bytes, Type.DICTIONARY);
  }
}
