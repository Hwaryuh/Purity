package kr.hwaryuh.purity.velocity.guard

import com.velocitypowered.api.event.permission.PermissionsSetupEvent
import com.velocitypowered.api.permission.PermissionFunction
import com.velocitypowered.api.permission.PermissionProvider
import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.api.proxy.Player
import kr.hwaryuh.poly.command.RESTRICTED_PERMISSION
import kr.hwaryuh.poly.framework.di.Lifecycle
import kr.hwaryuh.poly.framework.listener.HandleOrder
import kr.hwaryuh.poly.framework.listener.Listener
import kr.hwaryuh.poly.framework.listener.Subscribe
import kr.hwaryuh.purity.guard.GuardRules
import org.slf4j.Logger
import java.util.Collections
import java.util.WeakHashMap

@Listener
class VelocityGuardPermissions(
    private val rules: GuardRules,
    private val logger: Logger,
) : Lifecycle {
    @Volatile
    private var active = false
    private val pending = Collections.synchronizedMap(WeakHashMap<PermissionsSetupEvent, PermissionProvider>())

    override fun start() {
        active = true
    }

    @Subscribe(order = HandleOrder.LAST)
    fun onPermissions(event: PermissionsSetupEvent) {
        if (event.subject !is Player) return
        synchronized(pending) {
            if (pending.containsKey(event) || (event.provider as? RestrictedProvider)?.owner === this) return
            val wrapped = RestrictedProvider(event.provider)
            pending[event] = wrapped
            event.provider = wrapped
        }
    }

    @Subscribe(order = HandleOrder.MONITOR)
    fun onPermissionsComplete(event: PermissionsSetupEvent) {
        val expected = pending.remove(event) ?: return
        if (event.provider !== expected) logger.warn("Restricted permission provider was replaced after LAST")
    }

    private inner class RestrictedProvider(
        private val delegate: PermissionProvider,
    ) : PermissionProvider {
        val owner = this@VelocityGuardPermissions

        override fun createFunction(subject: com.velocitypowered.api.permission.PermissionSubject): PermissionFunction {
            val original = delegate.createFunction(subject)
            val player = subject as? Player ?: return original
            val id = player.uniqueId
            return PermissionFunction { permission ->
                if (active && permission == RESTRICTED_PERMISSION && id in rules.snapshot.bypassPlayers) {
                    Tristate.TRUE
                } else {
                    original.getPermissionValue(permission)
                }
            }
        }
    }

    override fun stop() {
        active = false
        pending.clear()
    }
}
