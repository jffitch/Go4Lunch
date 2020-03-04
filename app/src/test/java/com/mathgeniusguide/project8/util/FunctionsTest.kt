package com.mathgeniusguide.project8.util

import com.mathgeniusguide.project8.util.Functions.chatTime
import com.mathgeniusguide.project8.util.Functions.timeDiff
import com.mathgeniusguide.project8.util.Functions.timeDelay
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class FunctionsTest {
    @Test
    fun timeDiffTest1() {
        val startTime = 12316
        val endTime = 20512
        val differenceExpected = 356
        val differenceActual = timeDiff(startTime, endTime)
        assertEquals(differenceExpected, differenceActual)
    }

    @Test
    fun timeDiffTest2() {
        val startTime = 20835
        val endTime = 30935
        val differenceExpected = 1500
        val differenceActual = timeDiff(startTime, endTime)
        assertEquals(differenceExpected, differenceActual)
    }

    @Test
    fun timeDiffTest3() {
        val startTime = 61947
        val endTime = 1625
        val differenceExpected = 1238
        val differenceActual = timeDiff(startTime, endTime)
        assertEquals(differenceExpected, differenceActual)
    }

    @Test
    fun timedelayTest1() {
        val startTime = "06:18:25"
        val endTime = "13:15:05"
        val differenceExpected = 25000.toLong()
        val differenceActual = timeDelay(startTime, endTime)
        assertEquals(differenceExpected, differenceActual)
    }

    @Test
    fun timedelayTest2() {
        val startTime = "21:38:17"
        val endTime = "01:31:37"
        val differenceExpected = 14000.toLong()
        val differenceActual = timeDelay(startTime, endTime)
        assertEquals(differenceExpected, differenceActual)
    }

    @Test
    fun timedelayTest3() {
        val startTime = "00:00:01"
        val endTime = "23:59:59"
        val differenceExpected = 86398.toLong()
        val differenceActual = timeDelay(startTime, endTime)
        assertEquals(differenceExpected, differenceActual)
    }

    @Test
    fun fixTimeTest1() {
        val original = "7"
        val formattedExpected = "07:00:00"
        val formattedActual = original.fixTime()
        assertEquals(formattedExpected, formattedActual)
    }

    @Test
    fun fixTimeTest2() {
        val original = "3::5"
        val formattedExpected = "03:00:05"
        val formattedActual = original.fixTime()
        assertEquals(formattedExpected, formattedActual)
    }

    @Test
    fun fixTimeTest3() {
        val original = "26:12:09"
        val formattedExpected = "02:12:09"
        val formattedActual = original.fixTime()
        assertEquals(formattedExpected, formattedActual)
    }

    @Test
    fun fixTimeTest4() {
        val original = "3:97:34"
        val formattedExpected = "04:37:34"
        val formattedActual = original.fixTime()
        assertEquals(formattedExpected, formattedActual)
    }

    @Test
    fun fixTimeTest5() {
        val original = "59:78:62"
        val formattedExpected = "12:19:02"
        val formattedActual = original.fixTime()
        assertEquals(formattedExpected, formattedActual)
    }

    @Test
    fun chatTimeTest1() {
        val timestamp = "2020/03/01 14:38:15"
        val formattedDateExpected = "14:38"
        val formattedDateActual = chatTime(timestamp, TestResources(), Date("2020/03/01 23:00:00"))
        assertEquals(formattedDateExpected, formattedDateActual)
    }

    @Test
    fun chatTimeTest2() {
        val timestamp = "2020/02/16 21:05:14"
        val formattedDateExpected = "February 16 at 21:05"
        val formattedDateActual = chatTime(timestamp, TestResources(), Date("2020/03/01 23:00:00"))
        assertEquals(formattedDateExpected, formattedDateActual)
    }

    @Test
    fun chatTimeTest3() {
        val timestamp = "2019/11/23 06:40:00"
        val formattedDateExpected = "November 23, 2019 at 6:40"
        val formattedDateActual = chatTime(timestamp, TestResources(), Date("2020/03/01 23:00:00"))
        assertEquals(formattedDateExpected, formattedDateActual)
    }

    @Test
    fun toExpirationTest1() {
        val timeString = "Open 24/7"
        val expirationExpected = "2020/03/02 14:00"
        val expirationActual = timeString.toExpiration(TestResources(), Date("2020/03/01 14:00:00"))
        assertEquals(expirationExpected, expirationActual)
    }

    @Test
    fun toExpirationTest2() {
        val timeString = "Closing Soon"
        val expirationExpected = "2020/03/01 14:00"
        val expirationActual = timeString.toExpiration(TestResources(), Date("2020/03/01 14:00:00"))
        assertEquals(expirationExpected, expirationActual)
    }

    @Test
    fun toExpirationTest3() {
        val timeString = "Open Until 21:15"
        val expirationExpected = "2020/03/01 21:15"
        val expirationActual = timeString.toExpiration(TestResources(), Date("2020/03/01 14:00:00"))
        assertEquals(expirationExpected, expirationActual)
    }

    @Test
    fun toExpirationTest4() {
        val timeString = "Opens At 6:02"
        val expirationExpected = "2020/03/02 06:02"
        val expirationActual = timeString.toExpiration(TestResources(), Date("2020/03/01 14:00:00"))
        assertEquals(expirationExpected, expirationActual)
    }
}