package kr.hwaryuh.purity.guard

import java.util.Locale

object GuardPolicy {
    fun isBlocked(
        label: String,
        blocked: Set<String>,
        exists: (String) -> Boolean,
    ): Boolean {
        val name = label.lowercase(Locale.ROOT)
        if (name in blocked) return true
        val colon = name.indexOf(':')
        if (colon >= 0) return name.substring(0, colon + 1) in blocked || name.substring(colon + 1) in blocked
        return blocked.any { it.endsWith(':') && exists(it + name) }
    }
}
