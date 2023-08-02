package blippy.metainfo

import blippy.crypto.Hashing
import blippy.encoding.Bencoding
import blippy.metainfo.Info.MultipleFileInfo
import blippy.metainfo.Info.SingleFileInfo
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException

class MetainfoDecoder {

    @Suppress("UNCHECKED_CAST")
    fun decode(torrent: InputStream): Metainfo {
        try {
            torrent.use {
                val metainfo = Bencoding.decodeDictionary(torrent.readAllBytes())
                val announce = URI(metainfo["announce"] as String)
                val infoDictionary = metainfo["info"] as Map<String, Any>
                val infoHash = Hashing.sha1(Bencoding.encodeMap(infoDictionary))
                val info: Info = if (infoDictionary.containsKey("files")) {
                    // Multiple File Mode
                    val files =
                        (infoDictionary["files"] as List<Map<String, Any>>)
                            .stream()
                            .map { fileDictionary: Map<String, Any> ->
                                val length = fileDictionary["length"] as Long
                                val path = fileDictionary["path"] as List<String>
                                MultipleFileInfo.File(length, path)
                            }
                            .toList()
                    MultipleFileInfo(infoDictionary, infoHash, files)
                } else {
                    // Single File Mode
                    SingleFileInfo(infoDictionary, infoHash)
                }
                return Metainfo(announce, info)
            }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        } catch (ex: URISyntaxException) {
            throw RuntimeException(ex)
        }
    }
}
