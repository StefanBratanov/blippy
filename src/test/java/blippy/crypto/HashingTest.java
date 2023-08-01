package blippy.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HashingTest {

  @Test
  void hashesInputToSha1() {
    final byte[] result = Hashing.sha1("foobar".getBytes());
    assertThat(result)
        .hasSize(20)
        .asHexString()
        .isEqualTo("8843D7F92416211DE9EBB963FF4CE28125932878");
  }
}
