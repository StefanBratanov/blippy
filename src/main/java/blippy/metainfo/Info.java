package blippy.metainfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

public abstract class Info {

  protected final Map<String, Object> dictionary;
  protected final byte[] infoHash;
  protected final long pieceLength;
  protected final String pieces;
  protected final String name;

  public Info(final Map<String, Object> dictionary, final byte[] infoHash) {
    this.dictionary = dictionary;
    this.infoHash = infoHash;
    pieceLength = (long) dictionary.get("piece length");
    pieces = (String) dictionary.get("pieces");
    name = (String) dictionary.get("name");
  }

  public Optional<SingleFileInfo> toSingleFileInfo() {
    if (this instanceof SingleFileInfo) {
      return Optional.of((SingleFileInfo) this);
    }
    return Optional.empty();
  }

  public Optional<MultipleFileInfo> toMultipleFileInfo() {
    if (this instanceof MultipleFileInfo) {
      return Optional.of((MultipleFileInfo) this);
    }
    return Optional.empty();
  }

  public byte[] getInfoHash() {
    return infoHash;
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

  public static class SingleFileInfo extends Info {

    private final long length;

    public SingleFileInfo(final Map<String, Object> dictionary, final byte[] infoHash) {
      super(dictionary, infoHash);
      this.length = (long) dictionary.get("length");
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

    public MultipleFileInfo(
        final Map<String, Object> dictionary, final byte[] infoHash, final List<File> files) {
      super(dictionary, infoHash);
      this.files = files;
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
