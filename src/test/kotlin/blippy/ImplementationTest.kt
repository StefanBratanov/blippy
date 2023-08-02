package blippy

import blippy.metainfo.MetainfoDecoder
import blippy.tracker.AnnounceResponse
import blippy.tracker.Tracker
import blippy.tracker.UdpTrackerClient
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.InetAddress
import java.time.Duration

internal class ImplementationTest {
    @Test
    @Disabled("Only for manual testing. Shouldn't run in CI.")
    @Throws(
        IOException::class,
    )
    fun testImplementation() {
        val torrent = TestUtil.readResource("avengers.torrent")
        val metainfoDecoder = MetainfoDecoder()
        val metainfo = metainfoDecoder.decode(torrent)
        println("Metainfo: $metainfo")
        val trackerUri = metainfo.announce
        val tracker = Tracker(
            InetAddress.getByName(trackerUri.host),
            trackerUri.path,
            trackerUri.port,
        )
        val udpTrackerClient = UdpTrackerClient(tracker, metainfo)
        Assertions.assertThat(udpTrackerClient.connectAndAnnounce())
            .succeedsWithin(Duration.ofSeconds(5))
            .satisfies(
                ThrowingConsumer { (_, _, seeders, peers): AnnounceResponse ->
                    Assertions.assertThat(seeders).isGreaterThan(0)
                    Assertions.assertThat(peers).isNotEmpty()
                },
            )
    }
}
