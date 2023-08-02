package blippy.metainfo

import java.util.*

abstract class Info(
    dictionary: Map<String, Any>,
    val infoHash: ByteArray?,
) {
    val pieceLength: Long = dictionary["piece length"] as Long
    val pieces: String = dictionary["pieces"] as String
    val name: String = dictionary["name"] as String

    fun toSingleFileInfo(): Optional<SingleFileInfo> {
        return if (this is SingleFileInfo) {
            Optional.of(this)
        } else {
            Optional.empty()
        }
    }

    fun toMultipleFileInfo(): Optional<MultipleFileInfo> {
        return if (this is MultipleFileInfo) {
            Optional.of(this)
        } else {
            Optional.empty()
        }
    }

    class SingleFileInfo(dictionary: Map<String, Any>, infoHash: ByteArray) :
        Info(dictionary, infoHash) {
        val length: Long

        init {
            length = dictionary["length"] as Long
        }

        override fun toString(): String {
            return StringJoiner(", ", SingleFileInfo::class.java.simpleName + "[", "]")
                .add("pieceLength=$pieceLength")
                .add("name='$name'")
                .add("length=$length")
                .toString()
        }
    }

    class MultipleFileInfo(
        dictionary: Map<String, Any>,
        infoHash: ByteArray,
        val files: List<File>,
    ) : Info(dictionary, infoHash) {

        override fun toString(): String {
            return StringJoiner(", ", MultipleFileInfo::class.java.simpleName + "[", "]")
                .add("pieceLength=$pieceLength")
                .add("name='$name'")
                .add("files=$files")
                .toString()
        }

        data class File(val length: Long, val path: List<String>)
    }
}
