package kr.hwaryuh.purity.fingerprint

import kr.hwaryuh.purity.fingerprint.catalog.CLIENTS
import kr.hwaryuh.purity.fingerprint.catalog.LOADERS
import kr.hwaryuh.purity.fingerprint.catalog.MODS

// Channel and payload identifiers are unverified placeholders until traced on real clients.
// Sources: MoidAC (MIT, Branduzzo) for probe keys; MoidAC and ChecksMOD for cheat brands; ChecksMOD, ClientPolicy and ClientDetectorPlus for channel names.
// NoRisk, Alpine and Essential channels are taken from their official server API or mod sources.
val FINGERPRINTS: List<Fingerprint> = LOADERS + CLIENTS + MODS

val PROBES: List<Signal.Probe> = FINGERPRINTS.flatMap { it.signals }.filterIsInstance<Signal.Probe>().distinct()
