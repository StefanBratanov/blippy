package blippy.tracker

import blippy.metainfo.Metainfo
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.util.concurrent.Future
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/** [UDP Tracker Protocol for BitTorrent](https://www.bittorrent.org/beps/bep_0015.html) */
class UdpTrackerClient(private val tracker: Tracker, private val metainfo: Metainfo) {
    private val workGroup: EventLoopGroup

    init {
        workGroup = NioEventLoopGroup()
    }

    fun connectAndAnnounce(): CompletableFuture<AnnounceResponse> {
        val announceResponseFuture = CompletableFuture<AnnounceResponse>()
        val transactionId = 42
        val channelFuture =
            Bootstrap()
                .group(workGroup)
                .channel(NioDatagramChannel::class.java)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(
                    object : ChannelInitializer<DatagramChannel>() {
                        override fun initChannel(datagramChannel: DatagramChannel) {
                            datagramChannel
                                .pipeline()
                                .addLast(UdpTrackerRequestHandler(metainfo, transactionId, announceResponseFuture))
                        }
                    },
                )
                .connect(tracker.address, tracker.port)
                .addListener(
                    ChannelFutureListener { connect: ChannelFuture ->
                        if (connect.isSuccess) {
                            val action = 0
                            val connectRequest = Unpooled.buffer(16)
                            connectRequest.writeLong(PROTOCOL_ID)
                            connectRequest.writeInt(action)
                            connectRequest.writeInt(transactionId)
                            connect.channel().writeAndFlush(connectRequest)
                                .addListener { sentRequest: Future<in Void?> ->
                                    if (sentRequest.isSuccess) {
                                        LOG.info(
                                            "Sent connect request to {} via UDP port {}",
                                            tracker.address,
                                            tracker.port,
                                        )
                                    } else {
                                        announceResponseFuture.completeExceptionally(sentRequest.cause())
                                        // Shutdown the event loop group on failure
                                        workGroup.shutdownGracefully()
                                    }
                                }
                        } else {
                            announceResponseFuture.completeExceptionally(connect.cause())
                            // Shutdown the event loop group on failure
                            workGroup.shutdownGracefully()
                        }
                    },
                )
        return announceResponseFuture.whenComplete { _, _ ->
            // Close the channel when the future completes (either success or failure)
            if (channelFuture.channel().isOpen) {
                channelFuture.channel().close()
            }
            // Shutdown the event loop group after completing the future
            workGroup.shutdownGracefully()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(UdpTrackerClient::class.java)
        private const val PROTOCOL_ID = 0x41727101980L // magic constant
    }
}
