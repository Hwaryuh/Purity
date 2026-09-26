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
        loader("NEOFORGE") {
            brand("neoforge*")
            channel("neoforge:*")
            payload("neoforge:*")
        }
    }
