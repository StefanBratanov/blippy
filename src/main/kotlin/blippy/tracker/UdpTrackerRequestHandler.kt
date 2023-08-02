package blippy.tracker

import blippy.metainfo.Metainfo
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.DatagramPacket
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.concurrent.CompletableFuture

class UdpTrackerRequestHandler(
    private val metainfo: Metainfo,
    private val transactionId: Int,
    private val announceResponseFuture: CompletableFuture<AnnounceResponse>,
) : SimpleChannelInboundHandler<DatagramPacket>() {

    @Throws(NoSuchAlgorithmException::class, UnknownHostException::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
        val content = msg.content()
        val action = Action.entries[content.readInt()]
        LOG.info("Processing {} action from {}", action, msg.sender())
        when (action) {
            Action.CONNECT -> {
                val transactionId = content.readInt()
                if (transactionId != this.transactionId) {
                    ctx.fireExceptionCaught(
                        IllegalArgumentException("Received transaction id is not valid: $transactionId"),
                    )
                }
                val connectionId = content.readLong()
                val announceRequest = Unpooled.buffer(98)
                announceRequest.writeLong(connectionId)
                announceRequest.writeInt(Action.ANNOUNCE.ordinal)
                announceRequest.writeInt(transactionId)
                announceRequest.writeBytes(metainfo.info.infoHash)
                val peerId = ByteArray(20)
                SecureRandom.getInstanceStrong().nextBytes(peerId)
                announceRequest.writeBytes(peerId)
                // downloaded
                announceRequest.writeLong(0)
                // left
                announceRequest.writeLong(0)
                // uploaded
                announceRequest.writeLong(0)
                // event
                announceRequest.writeInt(0)
                // IP address
                announceRequest.writeInt(0)
                // key
                announceRequest.writeInt(42)
                // num_want
                announceRequest.writeInt(-1)
                // port
                announceRequest.writeShort(69)
                ctx.writeAndFlush(announceRequest)
            }

            Action.ANNOUNCE -> {
                val transactionId = content.readInt()
                if (transactionId != this.transactionId) {
                    ctx.fireExceptionCaught(
                        IllegalArgumentException("Received transaction id is not valid: $transactionId"),
                    )
                }
                val interval = content.readInt()
                val leechers = content.readInt()
                val seeders = content.readInt()
                // Decode peer information
                val peers: MutableList<Peer> = ArrayList()
                var peerInfoOffset = 20
                while (peerInfoOffset < content.readableBytes()) {
                    // Decode IP address (4 bytes)
                    val ipAddressBuilder = StringBuilder()
                    for (i in 0..3) {
                        val octet = content.getUnsignedByte(peerInfoOffset + i).toInt()
                        ipAddressBuilder.append(octet)
                        if (i < 3) {
                            ipAddressBuilder.append(".")
                        }
                    }
                    val ipAddress = ipAddressBuilder.toString()
                    // Decode port (2 bytes)
                    val port = content.getUnsignedShort(peerInfoOffset + 4)
                    peerInfoOffset += 6 // Move to the next peer information
                    peers.add(Peer(InetAddress.getByName(ipAddress), port))
                }
                val announceResponse = AnnounceResponse(interval, leechers, seeders, peers)
                LOG.info(
                    "Received an announce response consisting of {} peers",
                    announceResponse.peers.size,
                )
                announceResponseFuture.complete(announceResponse)
            }

            Action.ERROR -> {
                val transactionId = content.readInt()
                if (transactionId != this.transactionId) {
                    ctx.fireExceptionCaught(
                        IllegalArgumentException("Received transaction id is not valid: $transactionId"),
                    )
                }
                val message = content.readCharSequence(content.readableBytes(), StandardCharsets.UTF_8)
                ctx.fireExceptionCaught(IllegalStateException(message.toString()))
            }

            else -> throw UnsupportedOperationException("$action action is not supported")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(UdpTrackerRequestHandler::class.java)
    }
}
