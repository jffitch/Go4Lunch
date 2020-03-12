package com.mathgeniusguide.project8.responses.autocomplete

data class AutocompleteResponse(
    val predictions: List<AutocompleteItem>,
    val status: String
)