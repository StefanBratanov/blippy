package blippy.metainfo;

import java.net.URI;

/**
 * <a href="https://wiki.theory.org/BitTorrentSpecification#Metainfo_File_Structure">Metainfo File
 * Structure</a>
 */
public record Metainfo(URI announce, Info info) {}
