package blippy.metainfo

import blippy.TestUtil
import blippy.metainfo.Info.MultipleFileInfo
import blippy.metainfo.Info.SingleFileInfo
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class MetainfoDecoderTest {
    private val metainfoDecoder = MetainfoDecoder()

    @Test
    fun decodesMetainfoFromTorrentWithSingleFile() {
        val torrent = TestUtil.readResource("single_file.torrent")
        val (announce, info1) = metainfoDecoder.decode(torrent)
        Assertions.assertThat(announce).hasToString("udp://tracker.coppersurfer.tk:80/announce")
        Assertions.assertThat(info1.infoHash)
            .asHexString()
            .isEqualTo("1631C8F603D38D6DA727B5A4F25DB9D6D5352476")
        Assertions.assertThat(info1.toSingleFileInfo())
            .hasValueSatisfying { info: SingleFileInfo ->
                Assertions.assertThat(info.pieceLength).isEqualTo(524288L)
                Assertions.assertThat(info.pieces).isNotEmpty()
                Assertions.assertThat(info.name)
                    .isEqualTo("Liar.Game.S01E06.720p.WEB.x264-WaLMaRT[eztv].mkv")
                Assertions.assertThat(info.length).isEqualTo(1003682751L)
            }
    }

    @Test
    fun decodesMetainfoFromTorrentWithMultipleFiles() {
        val torrent = TestUtil.readResource("multiple_file.torrent")
        val (announce, info1) = metainfoDecoder.decode(torrent)
        Assertions.assertThat(announce)
            .hasToString("udp://tracker.leechers-paradise.org:6969/announce")
        Assertions.assertThat(info1.infoHash)
            .asHexString()
            .isEqualTo("E2B3943A8AE663B623BF6C719C08A3843E24B6DB")
        Assertions.assertThat(info1.toMultipleFileInfo())
            .hasValueSatisfying { info: MultipleFileInfo ->
                Assertions.assertThat(info.pieceLength).isEqualTo(4194304L)
                Assertions.assertThat(info.pieces).isNotEmpty()
                Assertions.assertThat(info.name)
                    .isEqualTo(
                        "Glass.Onion.A.Knives.Out.Mystery.2022.1080p.NF.WEB-DL.DDP5.1.Atmos.H.264-ShiNobi[TGx]",
                    )
                Assertions.assertThat(info.files).hasSize(3)
            }
    }
}
