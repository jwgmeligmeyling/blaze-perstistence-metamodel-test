package extensions

import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.getOrSet

class Counter {
    companion object {
        val COUNTER = ThreadLocal<AtomicInteger>()

        fun getCount() : Byte {
            return Integer.toUnsignedLong(COUNTER.getOrSet { AtomicInteger() }.incrementAndGet()).toByte()
        }
    }
}