package kr.hwaryuh.purity.velocity.guard

import kr.hwaryuh.poly.framework.di.Bean
import kr.hwaryuh.poly.framework.di.Lifecycle
import kr.hwaryuh.poly.framework.di.Reloadable
import kr.hwaryuh.purity.guard.GuardConfig

@Bean
class VelocityGuardSettings(
    private val guard: GuardConfig,
) : Lifecycle,
    Reloadable {
    override val id = "purity"

    override fun start() {
        reload()
    }

    override fun reload() {
        guard.snapshot = guard.read()
    }
}
