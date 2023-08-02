package blippy;

import static org.assertj.core.api.Assertions.assertThat;

import blippy.metainfo.Metainfo;
import blippy.metainfo.MetainfoDecoder;
import blippy.tracker.Tracker;
import blippy.tracker.UdpTrackerClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ImplementationTest {

  @Test
  @Disabled("Only for manual testing. Shouldn't run in CI.")
  void testImplementation() throws IOException {

    final InputStream torrent = TestUtil.readResource("avengers.torrent");

    final MetainfoDecoder metainfoDecoder = new MetainfoDecoder();

    final Metainfo metainfo = metainfoDecoder.decode(torrent);

    System.out.println("Metainfo: " + metainfo);

    final URI trackerUri = metainfo.announce();

    final Tracker tracker =
        new Tracker(
            InetAddress.getByName(trackerUri.getHost()),
            trackerUri.getPath(),
            trackerUri.getPort());

    final UdpTrackerClient udpTrackerClient = new UdpTrackerClient(tracker, metainfo);

    assertThat(udpTrackerClient.connectAndAnnounce())
        .succeedsWithin(Duration.ofSeconds(5))
        .satisfies(
            announceResponse -> {
              assertThat(announceResponse.seeders()).isGreaterThan(0);
              assertThat(announceResponse.peers()).isNotEmpty();
            });
  }
}
