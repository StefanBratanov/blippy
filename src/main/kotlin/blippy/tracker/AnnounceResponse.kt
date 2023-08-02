package blippy.tracker

data class AnnounceResponse(val interval: Int, val leechers: Int, val seeders: Int, val peers: List<Peer>)
