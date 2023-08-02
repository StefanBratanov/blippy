package blippy

import java.io.InputStream

object TestUtil {
    fun readResource(resource: String): InputStream {
        return Thread.currentThread().contextClassLoader.getResourceAsStream(resource)!!
    }
}
