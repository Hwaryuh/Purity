package kr.hwaryuh.purity.fingerprint.catalog

import kr.hwaryuh.purity.fingerprint.fingerprints

internal val CLIENTS =
    fingerprints {
        client("LUNAR", "Lunar Client") {
            brand("lunarclient*")
            channel("lunar:*", "lunarclient:*")
        }
        client("BADLION", "Badlion Client") {
            brand("badlion*")
            channel("badlion:*")
        }
        client("FEATHER", "Dawn (Feather Client)") {
            brand("feather*", "dawn*")
            channel("feather:*", "dawn:*", "dawn_bs:*")
        }
        client("LABYMOD", "LabyMod") {
            brand("*labymod*")
            channel("labymod:*", "labymod3:*")
        }
        client("AXOLOTL", "AxolotlClient") { channel("axolotlclient:*") }
        client("FIVEZIG", "5zig") { channel("the5zigmod:*") }
        client("NORISK", "NoRisk Client") { channel("norisk:*") }
        // "ac" is too generic to glob.
        client("ALPINE", "Alpine Client") { channel("ac:handshake", "ac:play") }

        client("METEOR", "Meteor Client") {
            brand("*meteor*")
            channel("meteor-client:*", "meteorclient:*", "meteor:client")
            keybind("key.meteor-client.open-gui")
        }
        client("WURST") {
            brand("*wurst*")
            channel("wurst:*")
            keybind("key.wurst.zoom")
        }
        client("ARISTOIS") {
            brand("*aristois*")
            channel("aristois:*")
            translation("emc.module.killaura.name")
        }
        client("IMPACT") {
            brand("*impact*")
            channel("impact:*")
            translation("impact.module.killaura.name")
        }
        client("LIQUIDBOUNCE", "LiquidBounce") {
            brand("*liquidbounce*")
            translation("liquidbounce.module.killaura.name")
        }
        client("INERTIA") {
            brand("*inertia*")
            translation("inertia.module.killaura.name")
        }
        client("BLEACHHACK", "BleachHack") {
            brand("*bleachhack*")
            translation("bleachhack.module.killaura")
        }
        client("RUSHERHACK", "RusherHack") {
            brand("*rusherhack*")
            translation("rusherhack.module.killaura.name")
        }
        client("LAMBDA") {
            brand("*lambda*")
            translation("lambda.module.killaura.name")
        }
        client("FUTURE") {
            brand("*future*")
            channel("future:*")
        }
        client("SIGMA") {
            brand("*sigma*")
            channel("sigma:*")
        }
        client("NOVOLINE") { brand("*novoline*") }
        client("VAPE") { brand("*vape*") }
        client("RAVEN") { brand("*raven*") }
        client("COFFEE", "Coffee Client") { translation("coffee.module.killaura.name") }
        client("KAMI_BLUE", "KAMI Blue") { translation("kami.module.killaura.name") }
        client("LUMINA") { keybind("key.lumina.open_click_gui") }
    }
