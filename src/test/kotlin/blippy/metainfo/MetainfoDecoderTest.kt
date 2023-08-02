package blippy.metainfo;

import static org.assertj.core.api.Assertions.assertThat;

import blippy.TestUtil;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class MetainfoDecoderTest {

  private final MetainfoDecoder metainfoDecoder = new MetainfoDecoder();

  @Test
  void decodesMetainfoFromTorrentWithSingleFile() {
    final InputStream torrent = TestUtil.readResource("single_file.torrent");

    final Metainfo metainfo = metainfoDecoder.decode(torrent);

    assertThat(metainfo.announce()).hasToString("udp://tracker.coppersurfer.tk:80/announce");

    assertThat(metainfo.info().infoHash)
        .asHexString()
        .isEqualTo("1631C8F603D38D6DA727B5A4F25DB9D6D5352476");
    assertThat(metainfo.info().toSingleFileInfo())
        .hasValueSatisfying(
            info -> {
              assertThat(info.getPieceLength()).isEqualTo(524288L);
              assertThat(info.getPieces()).isNotEmpty();
              assertThat(info.getName())
                  .isEqualTo("Liar.Game.S01E06.720p.WEB.x264-WaLMaRT[eztv].mkv");
              assertThat(info.getLength()).isEqualTo(1003682751L);
            });
  }

  @Test
  void decodesMetainfoFromTorrentWithMultipleFiles() {
    final InputStream torrent = TestUtil.readResource("multiple_file.torrent");

    final Metainfo metainfo = metainfoDecoder.decode(torrent);

    assertThat(metainfo.announce())
        .hasToString("udp://tracker.leechers-paradise.org:6969/announce");

    assertThat(metainfo.info().infoHash)
        .asHexString()
        .isEqualTo("E2B3943A8AE663B623BF6C719C08A3843E24B6DB");
    assertThat(metainfo.info().toMultipleFileInfo())
        .hasValueSatisfying(
            info -> {
              assertThat(info.getPieceLength()).isEqualTo(4194304L);
              assertThat(info.getPieces()).isNotEmpty();
              assertThat(info.getName())
                  .isEqualTo(
                      "Glass.Onion.A.Knives.Out.Mystery.2022.1080p.NF.WEB-DL.DDP5.1.Atmos.H.264-ShiNobi[TGx]");
              assertThat(info.getFiles()).hasSize(3);
            });
  }
}
