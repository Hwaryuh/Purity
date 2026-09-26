package kr.hwaryuh.purity.fingerprint.catalog

import kr.hwaryuh.purity.fingerprint.fingerprints

internal val MODS =
    fingerprints {
        mod("LITEMATICA") { channel("litematica:*") }
        mod("SYNCMATICA") { channel("syncmatica:*") }
        mod("WORLDEDIT_CUI") { channel("worldedit:cui") }
        mod("XAEROS_MINIMAP") {
            channel("xaerominimap:*")
            translation("xaero.map.gui")
        }
        mod("XAEROS_WORLDMAP") {
            channel("xaeroworldmap:*")
            keybind("key.xaero.toggle_world_map")
        }
        mod("JOURNEYMAP") {
            channel("journeymap:*")
            keybind("journeymap.key.map")
        }
        mod("VOXELMAP") { channel("worldinfo:world_id") }
        mod("BARITONE") {
            channel("baritone:*")
            keybind("key.baritone.cancel")
        }
        mod("REPLAYMOD") { channel("replaymod:*") }
        mod("CARPET") { channel("carpet:*") }
        mod("VIVECRAFT") { channel("vivecraft:*") }
        mod("VOICECHAT") { channel("voicechat:*") }
        mod("ESSENTIAL") { channel("essential:*") }
        mod("DISTANT_HORIZONS") { channel("distant_horizons:*", "distanthorizons:*") }
        mod("XRAY") {
            channel("xray:*")
            keybind("xray.config.toggle")
        }
        mod("FREECAM") { keybind("key.freecam.toggle") }
        mod("CHESTESP") { keybind("key.chestesp.toggle") }
        mod("KILLAURA") { keybind("key.killaura") }
        mod("AUTOFISH") { keybind("key.autofish.open_gui") }
        mod("AUTOSWITCH") { keybind("key.autoswitch.toggle") }
        mod("AUTOCLICKER") {
            translation("autoclicker-fabric.hud.holding")
            keybind("key.auto-clicker_.toggle")
        }
        mod("ANTIAFK") { translation("key.antiafk.toggle") }
        mod("WORLD_DOWNLOADER") {
            channel("wdl:*")
            translation("key.wdl.startStop")
        }
        mod("BETTERSPRINTING") { channel("bsm:*") }
        mod("OPSEC") { keybind("key.opsec.toggle") }
        mod("NO_CHAT_REPORTS") { keybind("nochatreports.key.toggle") }
        mod("TWEAKEROO") { keybind("tweakeroo.feature_toggle.name.tweakfreecamera") }
        mod("IRIS") { keybind("iris.keybind.toggleShaders") }
        mod("SODIUM") { translation("sodium.options.pages.quality") }
        mod("OPTIFINE") {
            brand("optifine*")
            keybind("of.key.zoom")
        }
    }
