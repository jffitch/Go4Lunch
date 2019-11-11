package com.mathgeniusguide.project8.responses

data class PlaceResponse(
    val html_attributions: List<Any>,
    val results: List<PlaceResult>,
    val status: String
)