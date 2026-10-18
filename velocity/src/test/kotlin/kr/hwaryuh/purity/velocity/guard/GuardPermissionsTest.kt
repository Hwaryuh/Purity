package kr.hwaryuh.purity.velocity.guard

import com.velocitypowered.api.event.permission.PermissionsSetupEvent
import com.velocitypowered.api.permission.PermissionFunction
import com.velocitypowered.api.permission.PermissionProvider
import com.velocitypowered.api.permission.PermissionSubject
import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.api.proxy.Player
import kr.hwaryuh.purity.guard.GuardOptions
import kr.hwaryuh.purity.guard.GuardRules
import kr.hwaryuh.purity.guard.GuardSnapshot
import org.slf4j.Logger
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class GuardPermissionsTest {
    @Test
    fun wrapperDelegatesAndUpdatesMembershipWithoutReplacingFunction() {
        val id = UUID.randomUUID()
        val player = fake<Player> { method, _ -> if (method == "getUniqueId") id else error(method) }
        val rules =
            object : GuardRules {
                override var snapshot = GuardSnapshot.of(GuardOptions(setOf(id)))
            }
        var created = 0
        val provider =
            PermissionProvider {
                created++
                PermissionFunction { permission -> if (permission == "other") Tristate.TRUE else Tristate.UNDEFINED }
            }
        val listener = VelocityGuardPermissions(rules, fake<Logger> { _, _ -> null })
        listener.start()
        val event = PermissionsSetupEvent(player, provider)
        listener.onPermissions(event)
        val wrapper = event.provider
        listener.onPermissions(event)
        assertSame(wrapper, event.provider)
        listener.onPermissionsComplete(event)
        val function = event.createFunction(player)
        assertEquals(1, created)
        assertEquals(Tristate.TRUE, function.getPermissionValue("poly.restricted"))
        assertEquals(Tristate.TRUE, function.getPermissionValue("other"))
        rules.snapshot = GuardSnapshot.of(GuardOptions())
        assertEquals(Tristate.UNDEFINED, function.getPermissionValue("poly.restricted"))
        rules.snapshot = GuardSnapshot.of(GuardOptions(setOf(id)))
        assertEquals(Tristate.TRUE, function.getPermissionValue("poly.restricted"))
        listener.stop()
        assertEquals(Tristate.UNDEFINED, function.getPermissionValue("poly.restricted"))
    }

    @Test
    fun consoleIsUntouchedAndProviderReplacementOnlyWarns() {
        val rules =
            object : GuardRules {
                override val snapshot = GuardSnapshot.of(GuardOptions())
            }
        var warnings = 0
        val listener =
            VelocityGuardPermissions(
                rules,
                fake<Logger> { method, _ ->
                    if (method == "warn") warnings++
                    null
                },
            )
        listener.start()
        val provider = PermissionProvider { PermissionFunction.ALWAYS_FALSE }
        val console = fake<PermissionSubject> { method, _ -> error(method) }
        val consoleEvent = PermissionsSetupEvent(console, provider)
        listener.onPermissions(consoleEvent)
        assertSame(provider, consoleEvent.provider)
        val player = fake<Player> { method, _ -> if (method == "getUniqueId") UUID.randomUUID() else error(method) }
        val event = PermissionsSetupEvent(player, provider)
        listener.onPermissions(event)
        assertNotSame(provider, event.provider)
        event.provider = provider
        listener.onPermissionsComplete(event)
        assertSame(provider, event.provider)
        assertEquals(1, warnings)
        listener.stop()
    }

    private inline fun <reified T> fake(noinline answer: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { proxy, method, args ->
            when (method.name) {
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.get(0)
                else -> answer(method.name, args ?: emptyArray())
            }
        } as T
}
