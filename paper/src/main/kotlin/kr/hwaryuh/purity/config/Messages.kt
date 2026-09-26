package kr.hwaryuh.purity.config

import kr.hwaryuh.purity.fingerprint.Subject
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.configuration.ConfigurationSection

// Kick messages per client locale. Lookup per message: exact locale (ko_kr), then same language (ko_*), then default.
class Messages(
    private val default: String,
    private val byLocale: Map<String, Map<Subject, String>>,
) {
    init {
        require(byLocale[default]?.keys == Subject.entries.toSet()) { "messages.$default must define every blocked-* message" }
    }

    fun get(
        locale: String?,
        subject: Subject,
    ): String {
        val exact = locale.orEmpty().lowercase()
        val language = exact.substringBefore('_')
        return byLocale[exact]?.get(subject)
            ?: byLocale.entries.firstNotNullOfOrNull { (key, messages) ->
                messages[subject].takeIf { key.substringBefore('_') == language }
            }
            ?: byLocale.getValue(default).getValue(subject)
    }

    // Tagged so the Velocity bridge disconnects instead of redirecting.
    fun kick(
        locale: String?,
        subject: Subject,
        id: String,
    ): Component =
        Component
            .text()
            .insertion(KICK_MARKER)
            .append(MiniMessage.miniMessage().deserialize(get(locale, subject), Placeholder.unparsed("id", id)))
            .build()

    companion object {
        // Must match PurityVelocity.KICK_MARKER.
        const val KICK_MARKER = "purity:kick"

        // messages.<blocked-subject>.<locale>
        fun from(section: ConfigurationSection?): Messages {
            requireNotNull(section) { "messages section is missing" }
            val byLocale = mutableMapOf<String, MutableMap<Subject, String>>()
            for (subject in Subject.entries) {
                val translations = section.getConfigurationSection("blocked-${subject.name.lowercase()}") ?: continue
                for (locale in translations.getKeys(false)) {
                    byLocale.getOrPut(locale.lowercase()) { mutableMapOf() }[subject] = translations.getString(locale)!!
                }
            }
            return Messages(section.getString("default", "ko_kr")!!.lowercase(), byLocale)
        }
    }
}
