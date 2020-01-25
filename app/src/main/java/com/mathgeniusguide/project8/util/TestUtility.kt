package com.mathgeniusguide.project8.util

import com.mathgeniusguide.project8.responses.details.*
import java.util.*
import kotlin.collections.ArrayList

object TestUtility {
    fun getTestingDetailsResultListOfSize(size: Int): List<DetailsResult> {
        val list = ArrayList<DetailsResult>()
        for (i in 0 until size) {
            val item = DetailsResult(
                formatted_address = generateRandomString(),
                formatted_phone_number = generateRandomString(),
                geometry = getTestingDetailsGeometryListOfSize(1)[0],
                name = generateRandomString(),
                opening_hours = getTestingDetailsOpeningHoursListOfSize(1)[0],
                photos = getTestingDetailsPhotoListOfSize(5) as List,
                place_id = generateRandomString(),
                rating = generateRandomDouble(),
                website = generateRandomString()
            )
            list.add(item)
        }
        return list
    }

    fun getTestingDetailsGeometryListOfSize(size: Int): List<DetailsGeometry> {
        val list = ArrayList<DetailsGeometry>()
        for (i in 0 until size) {
            val item = DetailsGeometry(
                location = getTestingDetailsLocationListOfSize(1)[0]
            )
            list.add(item)
        }
        return list
    }

    fun getTestingDetailsLocationListOfSize(size: Int): List<DetailsLocation> {
        val list = ArrayList<DetailsLocation>()
        for (i in 0 until size) {
            val item = DetailsLocation(
                lat = generateRandomDouble(),
                lng = generateRandomDouble()
            )
            list.add(item)
        }
        return list
    }

    fun getTestingDetailsOpeningHoursListOfSize(size: Int): List<DetailsOpeningHours> {
        val list = ArrayList<DetailsOpeningHours>()
        for (i in 0 until size) {
            val item = DetailsOpeningHours(
                open_now = generateRandomBoolean(),
                periods = getTestingDetailsPeriodListOfSize(5),
                weekday_text = generateRandomStringListOfSize(5)
            )
            list.add(item)
        }
        return list
    }

    fun getTestingDetailsPeriodListOfSize(size: Int): List<DetailsPeriod> {
        val list = ArrayList<DetailsPeriod>()
        for (i in 0 until size) {
            val item = DetailsPeriod(
                close = getTestingDetailsCloseListOfSize(1)[0],
                open = getTestingDetailsOpenListOfSize(1)[0]
            )
            list.add(item)
        }
        return list
    }

    fun getTestingDetailsCloseListOfSize(size: Int): List<DetailsClose> {
        val list = ArrayList<DetailsClose>()
        for (i in 0 until size) {
            val item = DetailsClose(
                day = generateRandomInt(),
                time = generateRandomString()
            )
            list.add(item)
        }
        return list
    }

    fun getTestingDetailsOpenListOfSize(size: Int): List<DetailsOpen> {
        val list = ArrayList<DetailsOpen>()
        for (i in 0 until size) {
            val item = DetailsOpen(
                day = generateRandomInt(),
                time = generateRandomString()
            )
            list.add(item)
        }
        return list
    }

    fun getTestingDetailsPhotoListOfSize(size: Int): List<DetailsPhoto> {
        val list = ArrayList<DetailsPhoto>()
        for (i in 0 until size) {
            val item = DetailsPhoto(
                height = generateRandomInt(),
                html_attributions = generateRandomStringListOfSize(5),
                photo_reference = generateRandomString(),
                width = generateRandomInt()
            )
            list.add(item)
        }
        return list
    }

    fun generateRandomStringListOfSize(size: Int) : List<String> {
        val list = ArrayList<String>()
        for (i in 0 until size) {
            val item = generateRandomString()
            list.add(item)
        }
        return list
    }

    fun generateRandomString(): String {
        return UUID.randomUUID().toString()
    }

    fun generateRandomDouble(): Double {
        return Math.random() * 100
    }

    fun generateRandomBoolean(): Boolean {
        return Math.random() > .5
    }

    fun generateRandomInt(): Int {
        return (Math.random() * 100).toInt()
    }
}