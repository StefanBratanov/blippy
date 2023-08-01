package blippy.metainfo;

import blippy.encoding.Bencoding;
import blippy.metainfo.Metainfo.Info;
import blippy.metainfo.Metainfo.MultipleFileInfo;
import blippy.metainfo.Metainfo.MultipleFileInfo.File;
import blippy.metainfo.Metainfo.SingleFileInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class MetainfoDecoder {

  public MetainfoDecoder() {}

  @SuppressWarnings("unchecked")
  public Metainfo decode(final InputStream torrent) {
    try (torrent) {
      final Map<String, Object> metainfo = Bencoding.decodeDictionary(torrent.readAllBytes());
      final URI announce = new URI((String) metainfo.get("announce"));
      final Map<String, Object> infoDictionary = (Map<String, Object>) metainfo.get("info");
      final Info info;
      if (infoDictionary.containsKey("files")) {
        // Multiple File Mode
        final List<File> files =
            ((List<Map<String, Object>>) infoDictionary.get("files"))
                .stream()
                    .map(
                        fileDictionary -> {
                          final long length = (long) fileDictionary.get("length");
                          final List<String> path = (List<String>) fileDictionary.get("path");
                          return new File(length, path);
                        })
                    .toList();
        info = new MultipleFileInfo(infoDictionary, files);
      } else {
        // Single File Mode
        info = new SingleFileInfo(infoDictionary);
      }
      return new Metainfo(announce, info);
    } catch (final IOException | URISyntaxException ex) {
      throw new RuntimeException(ex);
    }
  }
}
