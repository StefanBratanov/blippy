package blippy.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

  public static byte[] sha1(final byte[] input) {
    try {
      final MessageDigest md = MessageDigest.getInstance("SHA-1");
      return md.digest(input);
    } catch (final NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
  }
}
