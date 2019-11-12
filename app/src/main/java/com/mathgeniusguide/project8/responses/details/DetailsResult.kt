package com.mathgeniusguide.project8.responses.details

data class DetailsResult(
    val address_components: List<DetailsAddressComponent>,
    val adr_address: String,
    val formatted_address: String,
    val formatted_phone_number: String,
    val geometry: DetailsGeometry,
    val icon: String,
    val id: String,
    val international_phone_number: String,
    val name: String,
    val opening_hours: DetailsOpeningHours,
    val photos: List<DetailsPhoto>,
    val place_id: String,
    val price_level: Int,
    val rating: Double,
    val reference: String,
    val reviews: List<DetailsReview>,
    val scope: String,
    val types: List<String>,
    val url: String,
    val user_ratings_total: Int,
    val utc_offset: Int,
    val vicinity: String,
    val website: String
)