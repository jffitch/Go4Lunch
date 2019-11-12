package com.mathgeniusguide.project8.responses.details

data class DetailsOpeningHours(
    val open_now: Boolean,
    val periods: List<DetailsPeriod>,
    val weekday_text: List<String>
)