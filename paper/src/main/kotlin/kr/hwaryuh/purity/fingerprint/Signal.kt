package kr.hwaryuh.purity.fingerprint

// UNKNOWN: a brand or identifier no fingerprint explains. Declared last so catalog detections are reported first.
enum class Subject { CLIENT, LOADER, MOD, UNKNOWN }

sealed interface Signal {
    // Case-insensitive; '*' matches any sequence.
    sealed interface Glob : Signal {
        val pattern: String
        val regex: Regex

        fun matches(value: String): Boolean = regex.matches(value.lowercase())
    }

    data class Brand(
        override val pattern: String,
    ) : Glob {
        override val regex = glob(pattern)

        override fun toString() = "brand:$pattern"
    }

    data class Channel(
        override val pattern: String,
    ) : Glob {
        override val regex = glob(pattern)

        override fun toString() = "channel:$pattern"
    }

    data class Payload(
        override val pattern: String,
    ) : Glob {
        override val regex = glob(pattern)

        override fun toString() = "payload:$pattern"
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

fun glob(pattern: String): Regex = Regex(pattern.lowercase().split('*').joinToString(".*") { Regex.escape(it) })
