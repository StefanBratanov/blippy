package blippy.metainfo

import java.net.URI

/**
 * [Metainfo File
 * Structure](https://wiki.theory.org/BitTorrentSpecification#Metainfo_File_Structure)
 */
data class Metainfo(val announce: URI, val info: Info)
