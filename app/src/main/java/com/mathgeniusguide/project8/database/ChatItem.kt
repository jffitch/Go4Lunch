package com.mathgeniusguide.project8.database

class ChatItem {
    companion object Factory {
        fun create(): ChatItem = ChatItem()
    }
    var id: String? = null
    var from: String? = null
    var to: String? = null
    var text: String? = null
}