package com.mathgeniusguide.project8.util

import android.content.res.Resources
import com.google.firebase.database.DatabaseReference
import com.mathgeniusguide.go4lunch.database.RestaurantItem
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.ChatItem
import com.mathgeniusguide.project8.database.ChosenRestaurantItem
import com.mathgeniusguide.project8.responses.details.DetailsOpeningHours
import com.mathgeniusguide.project8.responses.details.DetailsResult
import java.text.SimpleDateFormat
import java.util.*

object Functions {
    fun chatTime(timeStamp: String?, resources: Resources): String {
        if (timeStamp ==  null) {
            return ""
        }
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val dateSdf = SimpleDateFormat("MMMM d", Locale.getDefault())
        val yearSdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val yearDateSdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val dateYearSdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("H:mm", Locale.getDefault())
        val today = yearDateSdf.parse(yearDateSdf.format(Date()))
        val timeStampDate = yearDateSdf.parse(timeStamp)
        val timeStampDateTime = sdf.parse(timeStamp)
        val time = timeSdf.format(timeStampDateTime)
        val date = if (yearSdf.format(timeStampDate) == yearSdf.format(today)) dateSdf.format(timeStampDate) else dateYearSdf.format(timeStampDate)
        if (dateYearSdf.format(today) == dateYearSdf.format(timeStampDate)) {
            return time
        }
        return String.format(resources.getString(R.string.date_at_time), date, time)
    }
    fun openTime(times: DetailsOpeningHours?, resources: Resources): String {
        if (times == null) {
            return resources.getString(R.string.open_24_7)
        }
        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_WEEK) - 1
        val todayHour = today.get(Calendar.HOUR_OF_DAY)
        val todayMinute = today.get(Calendar.MINUTE)
        val dayAndTime = "${todayDay}${todayHour}${todayMinute}".toInt()
        if (times.open_now) {
            val periods = times.periods.filter {it.close != null && it.close.day != null && it.close.time != null }.map { "${it.close!!.day}${it.close.time}".toInt() }.sorted()
            val periodsAfter = periods.filter { it > dayAndTime }
            val next = if (periodsAfter.isEmpty()) periods[0] else periodsAfter[0]
            val diff = timeDiff(dayAndTime, next)
            val nextHour = (next / 100) % 100
            val nextMinute = next % 100
            if (diff < 30) {
                return resources.getString(R.string.closing_soon)
            }
            if (diff < 1440) {
                return String.format(
                    resources.getString(R.string.open_until),
                    "${nextHour}:${nextMinute.toString().padStart(2, '0')}"
                )
            }
            return resources.getString(R.string.open_24)
        } else {
            val periods = times.periods.filter {it.close != null && it.close.day != null && it.close.time != null }.map { "${it.open!!.day}${it.open.time}".toInt() }
            val periodsAfter = periods.filter { it > dayAndTime }
            val next = if (periodsAfter.isEmpty()) periods[0] else periodsAfter[0]
            val diff = timeDiff(dayAndTime, next)
            val nextDay = next / 10000
            val nextHour = (next / 100) % 100
            val nextMinute = next % 100
            if (diff < 1440) {
                return String.format(
                    resources.getString(R.string.opens_at),
                    "${nextHour}:${nextMinute.toString().padStart(2, '0')}"
                )
            }
            if (listOf(-6, 1).contains(nextDay - todayDay)) {
                return String.format(
                    resources.getString(R.string.opens_tomorrow_at),
                    "${nextHour}:${nextMinute.toString().padStart(2, '0')}"
                )
            }
            return String.format(
                resources.getString(R.string.opens_day_at), resources.getString(
                    when (nextDay) {
                        0 -> R.string.sunday
                        1 -> R.string.monday
                        2 -> R.string.tuesday
                        3 -> R.string.wednesday
                        4 -> R.string.thursday
                        5 -> R.string.friday
                        else -> R.string.saturday
                    }
                ), "${nextHour}:${nextMinute.toString().padStart(2, '0')}"
            )
        }
    }

    fun timeDiff(start: Int, end: Int): Int {
        // inputs are 5 digit numbers in form dhhmm
        // convert to minutes, then subtract
        // add 10080 if negative
        val startMinutes = start - 40 * (start / 100) - 4560 * (start / 10000)
        val endMinutes = end - 40 * (end / 100) - 4560 * (end / 10000)
        return (if (endMinutes < startMinutes) 10080 else 0) + endMinutes - startMinutes
    }

    fun nearbyPlaceDetails(result: DetailsResult, latitude: Double, longitude: Double, resources: Resources): NearbyPlace {
        val nearbyPlace = NearbyPlace()
        nearbyPlace.id = result.place_id
        nearbyPlace.address = result.formatted_address
        nearbyPlace.phone = result.formatted_phone_number
        nearbyPlace.latitude = result.geometry.location.lat
        nearbyPlace.longitude = result.geometry.location.lng
        nearbyPlace.distance = coordinateDistance(
            latitude,
            longitude,
            nearbyPlace.latitude,
            nearbyPlace.longitude
        )
        nearbyPlace.website = result.website
        nearbyPlace.name = result.name
        nearbyPlace.rating = result.rating
        nearbyPlace.time = openTime(result.opening_hours, resources)
        if (result.photos != null && result.photos.isNotEmpty()) {
            nearbyPlace.image = result.photos[0].photo_reference
        }
        return nearbyPlace
    }

    fun nearbyPlaceDetails(result: RestaurantItem, latitude: Double, longitude: Double): NearbyPlace {
        val nearbyPlace = NearbyPlace()
        nearbyPlace.id = result.id
        nearbyPlace.address = result.address
        nearbyPlace.phone = result.phone
        nearbyPlace.latitude = result.latitude
        nearbyPlace.longitude = result.longitude
        nearbyPlace.distance = coordinateDistance(
            latitude,
            longitude,
            nearbyPlace.latitude,
            nearbyPlace.longitude
        )
        nearbyPlace.website = result.website
        nearbyPlace.name = result.name
        nearbyPlace.rating = result.rating
        nearbyPlace.time = result.time
        nearbyPlace.image = result.image
        return nearbyPlace
    }

    fun coordinateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
        val latDiff = Math.abs(lat1 - lat2)
        val lngDiff = Math.abs(lng1 - lng2) * Math.cos(lat1 * Math.PI / 180.0)
        return (111319.5 * Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)).toInt()
    }

    fun createRestaurant(username: String, restaurant: String, liked: String, photo: String, database: DatabaseReference) {
        val newItem = database.child(Constants.CHOSEN_RESTAURANTS).push()
        val chosenRestaurantItem = ChosenRestaurantItem.create()
        chosenRestaurantItem.id = newItem.key
        chosenRestaurantItem.username = username
        chosenRestaurantItem.restaurant = restaurant
        chosenRestaurantItem.liked = liked
        chosenRestaurantItem.photo = photo
        newItem.setValue(chosenRestaurantItem)
    }

    fun updateRestaurant(itemKey: String, restaurant: String, database: DatabaseReference) {
        val itemReference = database.child(Constants.CHOSEN_RESTAURANTS).child(itemKey)
        itemReference.child("restaurant").setValue(restaurant)
    }

    fun updateLiked(itemKey: String, liked: MutableList<String>, database: DatabaseReference) {
        val itemReference = database.child(Constants.CHOSEN_RESTAURANTS).child(itemKey)
        itemReference.child("liked").setValue(liked.joinToString(" , "))
    }

    fun deleteRestaurant(itemKey: String, database: DatabaseReference) {
        val itemReference = database.child(Constants.CHOSEN_RESTAURANTS).child(itemKey)
        itemReference.removeValue()
    }

    fun createChat(from: String, to: String, text: String, database: DatabaseReference) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val newItem = database.child(Constants.CHATS).push()
        val chatItem = ChatItem.create()
        chatItem.id = newItem.key
        chatItem.from = from
        chatItem.to = to
        chatItem.text = text
        chatItem.timestamp = timestamp
        newItem.setValue(chatItem)
    }

    fun deleteChat(itemKey: String, database: DatabaseReference) {
        val itemReference = database.child(Constants.CHATS).child(itemKey)
        itemReference.removeValue()
    }
}