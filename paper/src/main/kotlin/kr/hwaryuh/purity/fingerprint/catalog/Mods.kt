package kr.hwaryuh.purity.fingerprint.catalog

import kr.hwaryuh.purity.fingerprint.fingerprints

internal val MODS =
    fingerprints {
        mod("LITEMATICA") { channel("litematica:*") }
        mod("SYNCMATICA") { channel("syncmatica:*") }
        mod("WORLDEDIT_CUI", "WorldEdit CUI") { channel("worldedit:cui") }
        mod("XAEROS_MINIMAP", "Xaero's Minimap") {
            channel("xaerominimap:*")
            translation("xaero.map.gui")
        }
        mod("XAEROS_WORLDMAP", "Xaero's World Map") {
            channel("xaeroworldmap:*")
            keybind("key.xaero.toggle_world_map")
        }
        mod("JOURNEYMAP", "JourneyMap") {
            channel("journeymap:*")
            keybind("journeymap.key.map")
        }
        mod("VOXELMAP", "VoxelMap") { channel("worldinfo:world_id") }
        mod("BARITONE") {
            channel("baritone:*")
            keybind("key.baritone.cancel")
        }
        mod("REPLAYMOD", "ReplayMod") { channel("replaymod:*") }
        mod("CARPET") { channel("carpet:*") }
        mod("VIVECRAFT") { channel("vivecraft:*") }
        mod("VOICECHAT", "Simple Voice Chat") { channel("voicechat:*") }
        mod("ESSENTIAL") { channel("essential:*") }
        mod("DISTANT_HORIZONS", "Distant Horizons") { channel("distant_horizons:*", "distanthorizons:*") }
        mod("XRAY", "X-Ray") {
            channel("xray:*")
            keybind("xray.config.toggle")
        }
        mod("FREECAM") { keybind("key.freecam.toggle") }
        mod("CHESTESP", "ChestESP") { keybind("key.chestesp.toggle") }
        mod("KILLAURA", "KillAura") { keybind("key.killaura") }
        mod("AUTOFISH", "AutoFish") { keybind("key.autofish.open_gui") }
        mod("AUTOSWITCH", "AutoSwitch") { keybind("key.autoswitch.toggle") }
        mod("AUTOCLICKER", "AutoClicker") {
            translation("autoclicker-fabric.hud.holding")
            keybind("key.auto-clicker_.toggle")
        }
        mod("ANTIAFK", "AntiAFK") { translation("key.antiafk.toggle") }
        mod("WORLD_DOWNLOADER", "World Downloader") {
            channel("wdl:*")
            translation("key.wdl.startStop")
        }
        mod("BETTERSPRINTING", "Better Sprinting") { channel("bsm:*") }
        mod("OPSEC", "OpSec") { keybind("key.opsec.toggle") }
        mod("NO_CHAT_REPORTS", "No Chat Reports") { keybind("nochatreports.key.toggle") }
        mod("TWEAKEROO") { keybind("tweakeroo.feature_toggle.name.tweakfreecamera") }
        // Bobby (maxRenderDistance) and slider-unlocking mods.
        mod("EXTENDED_RENDER_DISTANCE") { viewDistanceAbove(33) }
        mod("IRIS") { keybind("iris.keybind.toggleShaders") }
        mod("SODIUM") { translation("sodium.options.pages.quality") }
        mod("OPTIFINE", "OptiFine") {
            brand("optifine*")
            keybind("of.key.zoom")
        }
    }
