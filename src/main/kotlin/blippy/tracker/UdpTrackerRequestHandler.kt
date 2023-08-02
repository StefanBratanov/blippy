package blippy.tracker;

import blippy.metainfo.Metainfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpTrackerRequestHandler extends SimpleChannelInboundHandler<DatagramPacket> {

  private static final Logger LOG = LoggerFactory.getLogger(UdpTrackerRequestHandler.class);

  private final Metainfo metainfo;
  private final int transactionId;
  private final CompletableFuture<AnnounceResponse> announceResponseFuture;

  public UdpTrackerRequestHandler(
      final Metainfo metainfo,
      final int transactionId,
      final CompletableFuture<AnnounceResponse> announceResponseFuture) {
    this.metainfo = metainfo;
    this.transactionId = transactionId;
    this.announceResponseFuture = announceResponseFuture;
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final DatagramPacket msg)
      throws NoSuchAlgorithmException, UnknownHostException {
    final ByteBuf content = msg.content();
    final Action action = Action.values()[content.readInt()];
    LOG.info("Processing {} action from {}", action, msg.sender());
    switch (action) {
      case CONNECT -> {
        int transactionId = content.readInt();
        if (transactionId != this.transactionId) {
          ctx.fireExceptionCaught(
              new IllegalArgumentException(
                  "Received transaction id is not valid: " + transactionId));
        }
        long connectionId = content.readLong();
        final ByteBuf announceRequest = Unpooled.buffer(98);
        announceRequest.writeLong(connectionId);
        announceRequest.writeInt(Action.ANNOUNCE.ordinal());
        announceRequest.writeInt(transactionId);
        announceRequest.writeBytes(metainfo.info().getInfoHash());
        final byte[] peerId = new byte[20];
        SecureRandom.getInstanceStrong().nextBytes(peerId);
        announceRequest.writeBytes(peerId);
        // downloaded
        announceRequest.writeLong(0);
        // left
        announceRequest.writeLong(0);
        // uploaded
        announceRequest.writeLong(0);
        // event
        announceRequest.writeInt(0);
        // IP address
        announceRequest.writeInt(0);
        // key
        announceRequest.writeInt(42);
        // num_want
        announceRequest.writeInt(-1);
        // port
        announceRequest.writeShort(69);
        ctx.writeAndFlush(announceRequest);
      }
      case ANNOUNCE -> {
        final int transactionId = content.readInt();
        if (transactionId != this.transactionId) {
          ctx.fireExceptionCaught(
              new IllegalArgumentException(
                  "Received transaction id is not valid: " + transactionId));
        }
        final int interval = content.readInt();
        final int leechers = content.readInt();
        final int seeders = content.readInt();
        // Decode peer information
        final List<Peer> peers = new ArrayList<>();
        int peerInfoOffset = 20;
        while (peerInfoOffset < content.readableBytes()) {
          // Decode IP address (4 bytes)
          StringBuilder ipAddressBuilder = new StringBuilder();
          for (int i = 0; i < 4; i++) {
            int octet = content.getUnsignedByte(peerInfoOffset + i);
            ipAddressBuilder.append(octet);
            if (i < 3) {
              ipAddressBuilder.append(".");
            }
          }
          String ipAddress = ipAddressBuilder.toString();
          // Decode port (2 bytes)
          int port = content.getUnsignedShort(peerInfoOffset + 4);

          peerInfoOffset += 6; // Move to the next peer information

          peers.add(new Peer(InetAddress.getByName(ipAddress), port));
        }

        final AnnounceResponse announceResponse =
            new AnnounceResponse(interval, leechers, seeders, peers);

        LOG.info(
            "Received an announce response consisting of {} peers",
            announceResponse.peers().size());

        announceResponseFuture.complete(announceResponse);
      }
      case ERROR -> {
        final int transactionId = content.readInt();
        if (transactionId != this.transactionId) {
          ctx.fireExceptionCaught(
              new IllegalArgumentException(
                  "Received transaction id is not valid: " + transactionId));
        }
        final CharSequence message =
            content.readCharSequence(content.readableBytes(), StandardCharsets.UTF_8);
        ctx.fireExceptionCaught(new IllegalStateException(message.toString()));
      }
      default -> throw new UnsupportedOperationException(action + " action is not supported");
    }
  }
}
