package blippy.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

  private static final ThreadLocal<MessageDigest> SHA1_MESSAGE_DIGEST_THREAD_LOCAL =
      ThreadLocal.withInitial(
          () -> {
            try {
              return MessageDigest.getInstance("SHA-1");
            } catch (final NoSuchAlgorithmException ex) {
              throw new IllegalStateException("SHA-1 algorithm not available", ex);
            }
          });

  public static byte[] sha1(final byte[] input) {
    final MessageDigest md = SHA1_MESSAGE_DIGEST_THREAD_LOCAL.get();
    return md.digest(input);
  }
}
