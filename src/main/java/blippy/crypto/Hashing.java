package blippy.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

  private static final ThreadLocal<MessageDigest> SHA1_MESSAGE_DIGEST_THREAD_LOCAL =
      ThreadLocal.withInitial(() -> getMessageDigest("SHA-1"));

  public static byte[] sha1(final byte[] input) {
    final MessageDigest md = SHA1_MESSAGE_DIGEST_THREAD_LOCAL.get();
    return md.digest(input);
  }

  private static MessageDigest getMessageDigest(final String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (final NoSuchAlgorithmException ex) {
      throw new IllegalStateException(algorithm + " algorithm not available", ex);
    }
  }
}
