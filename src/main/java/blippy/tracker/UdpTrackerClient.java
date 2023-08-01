package blippy.tracker;

import blippy.metainfo.Metainfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href="https://www.bittorrent.org/beps/bep_0015.html">UDP Tracker Protocol for BitTorrent</a>
 */
public class UdpTrackerClient {

  private static final Logger LOG = LoggerFactory.getLogger(UdpTrackerClient.class);

  private static final long PROTOCOL_ID = 0x41727101980L; // magic constant

  private final Tracker tracker;
  private final Metainfo metainfo;
  private final EventLoopGroup workGroup;

  public UdpTrackerClient(final Tracker tracker, final Metainfo metainfo) {
    this.tracker = tracker;
    this.metainfo = metainfo;
    this.workGroup = new NioEventLoopGroup();
  }

  public CompletableFuture<AnnounceResponse> connectAndAnnounce() {
    final CompletableFuture<AnnounceResponse> announceResponseFuture = new CompletableFuture<>();
    final int transactionId = 42;
    final ChannelFuture channelFuture =
        new Bootstrap()
            .group(workGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(
                new ChannelInitializer<DatagramChannel>() {
                  protected void initChannel(@NotNull final DatagramChannel datagramChannel) {
                    datagramChannel
                        .pipeline()
                        .addLast(
                            new UdpTrackerRequestHandler(
                                metainfo, transactionId, announceResponseFuture));
                  }
                })
            .connect(tracker.address(), tracker.port())
            .addListener(
                (ChannelFutureListener)
                    connect -> {
                      if (connect.isSuccess()) {
                        int action = 0;
                        final ByteBuf connectRequest = Unpooled.buffer(16);
                        connectRequest.writeLong(PROTOCOL_ID);
                        connectRequest.writeInt(action);
                        connectRequest.writeInt(transactionId);
                        connect
                            .channel()
                            .writeAndFlush(connectRequest)
                            .addListener(
                                sentRequest -> {
                                  if (sentRequest.isSuccess()) {
                                    LOG.info(
                                        "Sent connect request to {} via UDP port {}",
                                        tracker.address(),
                                        tracker.port());
                                  } else {
                                    announceResponseFuture.completeExceptionally(
                                        sentRequest.cause());
                                    // Shutdown the event loop group on failure
                                    workGroup.shutdownGracefully();
                                  }
                                });
                      } else {
                        announceResponseFuture.completeExceptionally(connect.cause());
                        // Shutdown the event loop group on failure
                        workGroup.shutdownGracefully();
                      }
                    });
    return announceResponseFuture.whenComplete(
        (__, ___) -> {
          // Close the channel when the future completes (either success or failure)
          if (channelFuture.channel().isOpen()) {
            channelFuture.channel().close();
          }
          // Shutdown the event loop group after completing the future
          workGroup.shutdownGracefully();
        });
  }
}
