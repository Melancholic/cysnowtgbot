package com.anagorny.cysnowbot.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.NullAndEmptySource

class RoadStatusTest {

    @ParameterizedTest
    @CsvSource(
        "OPEN for all Vehicles, OPEN",
        "'open for all vehicles', OPEN",
        "CLOSED, CLOSED",
        "'Road closed due to snow', CLOSED",
        "'4X4 or car with chains', AWD_OR_CHAINS_ONLY",
        "'Snow chains required', AWD_OR_CHAINS_ONLY",
        "'something the site has never said before', UNKNOWN",
    )
    fun `matches status by keyword, tolerant of wording changes`(text: String, expected: RoadStatus) {
        assertEquals(expected, RoadStatus.parseFromText(text))
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun `falls back to UNKNOWN for blank input`(text: String?) {
        assertEquals(RoadStatus.UNKNOWN, RoadStatus.parseFromText(text))
    }
}
