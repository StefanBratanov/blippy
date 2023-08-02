package blippy.tracker

import java.net.InetAddress

data class Tracker(val address: InetAddress, val path: String, val port: Int)
