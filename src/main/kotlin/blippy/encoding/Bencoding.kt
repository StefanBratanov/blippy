package blippy.encoding

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import java.nio.charset.StandardCharsets

object Bencoding {
    private val BENCODE = Bencode(StandardCharsets.ISO_8859_1)

    fun encodeMap(map: Map<*, *>): ByteArray {
        return BENCODE.encode(map)
    }

    fun decodeDictionary(bytes: ByteArray): Map<String, Any> {
        return BENCODE.decode(bytes, Type.DICTIONARY)
    }
}
