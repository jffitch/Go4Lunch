package com.mathgeniusguide.project8.responses.details

data class DetailsResult(
    val formatted_address: String,
    val formatted_phone_number: String,
    val geometry: DetailsGeometry,
    val name: String,
    val opening_hours: DetailsOpeningHours,
    val photos: List<DetailsPhoto>,
    val place_id: String,
    val rating: Double,
    // val reviews: List<DetailsReview>,
    val website: String
)