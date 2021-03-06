package com.mathgeniusguide.project8.responses.place

data class PlaceResponse(
    val results: List<PlaceResult>,
    val status: String,
    val next_page_token: String?
)