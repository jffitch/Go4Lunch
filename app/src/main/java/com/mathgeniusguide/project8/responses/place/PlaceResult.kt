package com.mathgeniusguide.project8.responses.place

data class PlaceResult(
    val geometry: PlaceGeometry,
    val icon: String,
    val id: String,
    val name: String,
    val photos: List<PlacePhoto>,
    val place_id: String,
    val rating: Double,
    val types: List<String>,
    val user_ratings_total: Int,
    val vicinity: String
)