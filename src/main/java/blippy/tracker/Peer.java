package blippy.tracker;

import java.net.InetAddress;

public record Peer(InetAddress address, int port) {}
