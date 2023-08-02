package blippy.tracker;

import java.util.List;

public record AnnounceResponse(int interval, int leechers, int seeders, List<Peer> peers) {}
