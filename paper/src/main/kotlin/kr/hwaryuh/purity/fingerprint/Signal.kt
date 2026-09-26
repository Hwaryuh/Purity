package kr.hwaryuh.purity.fingerprint

import java.util.concurrent.ConcurrentHashMap

// UNKNOWN: a brand or identifier no fingerprint explains. Declared last so catalog detections are reported first.
enum class Subject { CLIENT, LOADER, MOD, UNKNOWN }

sealed interface Signal {
    // Case-insensitive; '*' matches any sequence.
    sealed interface Glob : Signal {
        val pattern: String

        fun matches(value: String): Boolean = REGEX.getOrPut(pattern) { glob(pattern) }.matches(value.lowercase())
    }

    data class Brand(
        override val pattern: String,
    ) : Glob {
        override fun toString() = "brand:$pattern"
    }

    data class Channel(
        override val pattern: String,
    ) : Glob {
        override fun toString() = "channel:$pattern"
    }

    data class Payload(
        override val pattern: String,
    ) : Glob {
        override fun toString() = "payload:$pattern"
    }

    // Client-reported view distance. Vanilla caps the slider at 32 and accepts 33 from options.txt.
    data class ViewDistanceAbove(
        val limit: Int,
    ) : Signal {
        override fun toString() = "view-distance>$limit"
    }

    // Resolved client-side by the sign probe. Keys are exact and case-sensitive.
    sealed interface Probe : Signal {
        val key: String
    }

    data class Keybind(
        override val key: String,
    ) : Probe {
        override fun toString() = "keybind:$key"
    }

    data class Translation(
        override val key: String,
    ) : Probe {
        override fun toString() = "translate:$key"
    }
}

private val REGEX = ConcurrentHashMap<String, Regex>()

fun glob(pattern: String): Regex = Regex(pattern.lowercase().split('*').joinToString(".*") { Regex.escape(it) })
