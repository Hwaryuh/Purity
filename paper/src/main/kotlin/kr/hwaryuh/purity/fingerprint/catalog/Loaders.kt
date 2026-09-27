package kr.hwaryuh.purity.fingerprint.catalog

import kr.hwaryuh.purity.fingerprint.fingerprints

internal val LOADERS =
    fingerprints {
        loader("FABRIC") {
            brand("*fabric*")
            channel("fabric:*", "fabric-*")
            payload("fabric:*", "fabric-*")
        }
        loader("QUILT") { brand("quilt*") }
        loader("FORGE") {
            brand("forge", "fml*")
            channel("forge:*", "fml:*")
            payload("forge:*", "fml:*")
        }
        loader("NEOFORGE", "NeoForge") {
            brand("neoforge*")
            channel("neoforge:*")
            payload("neoforge:*")
        }
        // Answer to the c:version challenge; Fabric API, Quilt and NeoForge implement it. Fabric Loader alone does not.
        loader("MODDED_LOADER") {
            channel("c:*")
            payload("c:*")
        }
    }
