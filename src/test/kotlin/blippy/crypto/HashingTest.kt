package blippy.crypto

import blippy.crypto.Hashing.sha1
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class HashingTest {
    @Test
    fun hashesInputToSha1() {
        val result = sha1("foobar".toByteArray())
        Assertions.assertThat(result)
            .hasSize(20)
            .asHexString()
            .isEqualTo("8843D7F92416211DE9EBB963FF4CE28125932878")
    }
}
