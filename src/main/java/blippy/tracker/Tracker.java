package blippy.tracker;

import java.net.InetAddress;

public record Tracker(InetAddress address, String path, int port) {}
