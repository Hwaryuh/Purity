package kr.hwaryuh.purity

import kr.hwaryuh.purity.detection.Evidence
import kr.hwaryuh.purity.detection.Observation
import kr.hwaryuh.purity.detection.Verdict
import kr.hwaryuh.purity.fingerprint.Signal
import kr.hwaryuh.purity.fingerprint.Subject
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportTest {
    @Test
    fun describeKick() {
        val evidence =
            listOf(
                Evidence(Subject.LOADER, "FABRIC", Signal.Brand("*fabric*"), "fabric"),
                Evidence(Subject.MOD, "SODIUM", Signal.Translation("sodium.options.pages.quality"), "sodium.options.pages.quality"),
            )
        assertEquals(
            "Kicked Steve: LOADER FABRIC | brand \"fabric\" | also MOD SODIUM",
            describe(
                "Steve",
                Observation("fabric"),
                evidence,
                Verdict(Subject.LOADER, "FABRIC"),
                bypass = false,
                enforce = true,
                withChannels = false,
            ),
        )
    }
}
