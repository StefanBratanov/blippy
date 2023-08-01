package blippy;

import java.io.InputStream;

public class TestUtil {

  public static InputStream readResource(final String resource) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
  }
}
