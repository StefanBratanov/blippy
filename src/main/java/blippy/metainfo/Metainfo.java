package blippy.metainfo;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * <a href="https://wiki.theory.org/BitTorrentSpecification#Metainfo_File_Structure">Metainfo File
 * Structure</a>
 */
public record Metainfo(URI announce, Info info) {

  public abstract static class Info {

    public enum Mode {
      SINGLE_FILE,
      MULTIPLE_FILE
    }

    protected final Map<String, Object> dictionary;
    protected final long pieceLength;
    protected final String pieces;
    protected final String name;

    public Info(final Map<String, Object> dictionary) {
      this.dictionary = dictionary;
      pieceLength = (long) dictionary.get("piece length");
      pieces = (String) dictionary.get("pieces");
      name = (String) dictionary.get("name");
    }

    public abstract Mode getMode();

    public Map<String, Object> getDictionary() {
      return dictionary;
    }

    public long getPieceLength() {
      return pieceLength;
    }

    public String getPieces() {
      return pieces;
    }

    public String getName() {
      return name;
    }
  }

  public static class SingleFileInfo extends Info {

    private final long length;

    public SingleFileInfo(final Map<String, Object> dictionary) {
      super(dictionary);
      this.length = (long) dictionary.get("length");
    }

    @Override
    public Mode getMode() {
      return Mode.SINGLE_FILE;
    }

    public long getLength() {
      return length;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", SingleFileInfo.class.getSimpleName() + "[", "]")
          .add("pieceLength=" + pieceLength)
          .add("name='" + name + "'")
          .add("length=" + length)
          .toString();
    }
  }

  public static class MultipleFileInfo extends Info {

    private final List<File> files;

    public MultipleFileInfo(final Map<String, Object> dictionary, final List<File> files) {
      super(dictionary);
      this.files = files;
    }

    @Override
    public Mode getMode() {
      return Mode.MULTIPLE_FILE;
    }

    public List<File> getFiles() {
      return files;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", MultipleFileInfo.class.getSimpleName() + "[", "]")
          .add("pieceLength=" + pieceLength)
          .add("name='" + name + "'")
          .add("files=" + files)
          .toString();
    }

    public record File(long length, List<String> path) {}
  }
}
