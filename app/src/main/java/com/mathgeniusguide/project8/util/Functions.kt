package com.mathgeniusguide.project8.util

import android.content.Context
import android.content.res.Resources
import androidx.work.*
import com.mathgeniusguide.go4lunch.database.RestaurantRoomdbItem
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.RestaurantItem
import com.mathgeniusguide.project8.responses.details.DetailsOpeningHours
import com.mathgeniusguide.project8.responses.details.DetailsResult
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

object Functions {
    // translate chat timestamp into readable text
    fun chatTime(timeStamp: String?, resources: Resources, todayDate: Date): String {
        // if timestamp doesn't exist, leave blank
        if (timeStamp ==  null) {
            return ""
        }
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val dateSdf = SimpleDateFormat("MMMM d", Locale.getDefault())
        val yearSdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val yearDateSdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val dateYearSdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("H:mm", Locale.getDefault())
        val today = yearDateSdf.parse(yearDateSdf.format(todayDate))
        val timeStampDate = yearDateSdf.parse(timeStamp)
        val timeStampDateTime = sdf.parse(timeStamp)
        val time = timeSdf.format(timeStampDateTime)
        val date = if (yearSdf.format(timeStampDate) == yearSdf.format(today)) dateSdf.format(timeStampDate) else dateYearSdf.format(timeStampDate)
        // if today, display time only
        if (dateYearSdf.format(today) == dateYearSdf.format(timeStampDate)) {
            return time
        }
        // if not today, display date and time
        return String.format(resources.getString(R.string.date_at_time), date, time)
    }

    // translate opening hours from API into one phrase for next opening or closing time
    private fun openTime(times: DetailsOpeningHours?, resources: Resources, today: Calendar): String {
        // if null, open 24/7
        if (times == null) {
            return resources.getString(R.string.open_24_7)
        }
        val todayDay = today.get(Calendar.DAY_OF_WEEK) - 1
        val todayHour = today.get(Calendar.HOUR_OF_DAY)
        val todayMinute = today.get(Calendar.MINUTE)
        val dayAndTime = "${todayDay}${todayHour}${todayMinute}".toInt()
        if (times.open_now) {
            val periods = times.periods.filter {it.close != null && it.close.day != null && it.close.time != null }.map { "${it.close!!.day}${it.close.time}".toInt() }.sorted()
            val periodsAfter = periods.filter { it > dayAndTime }
            if (periods.isEmpty()) {
                return resources.getString(R.string.open_24_7)
            }
            val next = if (periodsAfter.isEmpty()) periods[0] else periodsAfter[0]
            val diff = timeDiff(dayAndTime, next)
            val nextHour = (next / 100) % 100
            val nextMinute = next % 100
            // if open now and closing in less than 30 minutes, "Closing Soon"
            if (diff < 30) {
                return resources.getString(R.string.closing_soon)
            }
            // if open now and closing in less than a day, "Open Until ${time}"
            if (diff < 1440) {
                return String.format(
                    resources.getString(R.string.open_until),
                    "${nextHour}:${nextMinute.toString().padStart(2, '0')}"
                )
            }
            // if open now and not closing in less than a day, "Open For Next 24 Hours"
            return resources.getString(R.string.open_24)
        } else {
            val periods = times.periods.filter {it.close != null && it.close.day != null && it.close.time != null }.map { "${it.open!!.day}${it.open.time}".toInt() }
            val periodsAfter = periods.filter { it > dayAndTime }
            val next = if (periodsAfter.isEmpty()) periods[0] else periodsAfter[0]
            val diff = timeDiff(dayAndTime, next)
            val nextDay = next / 10000
            val nextHour = (next / 100) % 100
            val nextMinute = next % 100
            // if closed now and opening in less than a day, "Opens At ${time}"
            if (diff < 1440) {
                return String.format(
                    resources.getString(R.string.opens_at),
                    "${nextHour}:${nextMinute.toString().padStart(2, '0')}"
                )
            }
            // if closed now and opening tomorrow after current time, "Opens Tomorrow At ${time}"
            if (listOf(-6, 1).contains(nextDay - todayDay)) {
                return String.format(
                    resources.getString(R.string.opens_tomorrow_at),
                    "${nextHour}:${nextMinute.toString().padStart(2, '0')}"
                )
            }
            // if closed now and not opening today or tomorrow, "Opens ${day} At ${time}"
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

    // calculate time difference between two times in dhhmm format
    fun timeDiff(start: Int, end: Int): Int {
        // convert to minutes, then subtract
        // add 10080 (minutes in week) if negative
        val startMinutes = start - 40 * (start / 100) - 4560 * (start / 10000)
        val endMinutes = end - 40 * (end / 100) - 4560 * (end / 10000)
        return (if (endMinutes < startMinutes) 10080 else 0) + endMinutes - startMinutes
    }

    // calculate time difference between two times in hh:mm:ss format
    fun timeDelay(start: String, end: String): Long {
        val startArray = start.split(":")
        val endArray = end.split(":")

        // calculate hour, minute, and second differences
        // add 86400 (seconds in day) if start later than end
        val dayDelay = if (start > end) 86400 else 0
        val hourDelay = (endArray[0].toInt() - startArray[0].toInt()) * 3600
        val minuteDelay = (endArray[1].toInt() - startArray[1].toInt()) * 60
        val secondDelay = endArray[2].toInt() - startArray[2].toInt()

        return (dayDelay + hourDelay + minuteDelay + secondDelay).toLong()
    }

    // for loading restaurants from API
    fun restaurantItemDetails(result: DetailsResult, latitude: Double, longitude: Double, resources: Resources): RestaurantItem {
        val restaurantItem = RestaurantItem()
        restaurantItem.id = result.place_id
        restaurantItem.address = result.formatted_address
        restaurantItem.phone = result.formatted_phone_number
        restaurantItem.latitude = result.geometry.location.lat
        restaurantItem.longitude = result.geometry.location.lng
        // use distance formula to calculate distance from current and destination locations
        restaurantItem.distance = coordinateDistance(
            latitude,
            longitude,
            restaurantItem.latitude,
            restaurantItem.longitude
        )
        restaurantItem.website = result.website
        restaurantItem.name = result.name
        restaurantItem.rating = result.rating
        // translate opening hours from API into one phrase for next opening or closing time
        restaurantItem.time = openTime(result.opening_hours, resources, Calendar.getInstance())
        // if a photo exists, load first photo
        if (result.photos != null && result.photos.isNotEmpty()) {
            restaurantItem.image = result.photos[0].photo_reference
        }
        return restaurantItem
    }

    // for loading restaurants from Room Database
    fun restaurantItemDetails(result: RestaurantRoomdbItem, latitude: Double, longitude: Double): RestaurantItem {
        val restaurantItem = RestaurantItem()
        restaurantItem.id = result.id
        restaurantItem.address = result.address!!
        restaurantItem.phone = result.phone
        restaurantItem.latitude = result.latitude!!
        restaurantItem.longitude = result.longitude!!
        // use distance formula to calculate distance from current and destination locations
        restaurantItem.distance = coordinateDistance(
            latitude,
            longitude,
            restaurantItem.latitude,
            restaurantItem.longitude
        )
        restaurantItem.website = result.website
        restaurantItem.name = result.name!!
        restaurantItem.rating = result.rating!!
        restaurantItem.time = result.time!!
        restaurantItem.image = result.image!!
        return restaurantItem
    }

    // use distance formula to calculate distance between two locations
    fun coordinateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
        val latDiff = abs(lat1 - lat2)
        val lngDiff = abs(lng1 - lng2) * cos(lat1 * Math.PI / 180.0)
        // 111319.5 is approximately earth's circumference in meters divided by 360, which would be the distance for each degree
        return (111319.5 * sqrt(latDiff * latDiff + lngDiff * lngDiff)).toInt()
    }

    // activate or deactivate notification based on turnOn parameter, triggering at triggerTime
    fun setNotificationAlarm(turnOn: Boolean, triggerTime: String, username: String, context: Context) {
        val workManager = WorkManager.getInstance(context)
        val dateSdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val time = timeSdf.format(Date())
        // name the work as the date that it will trigger
        // if triggerTime already passed today, it will trigger tomorrow
        val date = dateSdf.format(Date(Date().time + if (triggerTime < time) 86400000 else 0))

        val delay = timeDelay(time, triggerTime)
        if (turnOn) {
            // create notification if turnOn
            val constraints = Constraints.Builder()
                .build()
            val data = Data.Builder()
                .putString("username", username)
                .build()
            val work = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(date, ExistingWorkPolicy.KEEP, work)
        } else {
            // cancel notification if not turnOn
            workManager.cancelUniqueWork(date)
        }
    }
}