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
        // Answer to the c:version challenge; Fabric, Quilt and NeoForge all implement it.
        loader("MODDED_LOADER") {
            channel("c:*")
            payload("c:*")
        }
        loader("NEOFORGE") {
            brand("neoforge*")
            channel("neoforge:*")
            payload("neoforge:*")
        }
    }
