package kr.hwaryuh.purity.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import net.kyori.adventure.text.Component;

// Java because Velocity has no library loader for the Kotlin stdlib.
// Velocity redirects a backend kick to the next server in `try`; Purity kicks must end the connection instead.
public final class PurityVelocity {
    // Must match KICK_MARKER in the Paper plugin.
    static final String KICK_MARKER = "purity:kick";

    static boolean isPurityKick(Component reason) {
        return KICK_MARKER.equals(reason.insertion());
    }

    // Runs last so redirects set by other plugins are overridden.
    @Subscribe(priority = Short.MIN_VALUE)
    public void onKicked(KickedFromServerEvent event) {
        event.getServerKickReason()
                .filter(PurityVelocity::isPurityKick)
                .ifPresent(reason -> event.setResult(KickedFromServerEvent.DisconnectPlayer.create(reason)));
    }
}
