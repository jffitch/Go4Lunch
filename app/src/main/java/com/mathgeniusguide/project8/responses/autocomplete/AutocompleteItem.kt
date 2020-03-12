package com.mathgeniusguide.project8.responses.autocomplete

data class AutocompleteItem(
    val place_id: String,
    val types: List<String>,
    val structured_formatting: AutocompleteStructuredFormatting
)