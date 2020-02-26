package com.mathgeniusguide.project8.ui

import ChatAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.ChatItem
import com.mathgeniusguide.project8.util.FirebaseFunctions.createChat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.chat_fragment.*
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {
    lateinit var act: MainActivity
    var chatList = mutableListOf<ChatItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        act = activity as MainActivity
        act.toolbar.visibility = View.VISIBLE
        act.autocompleteFragment.view?.visibility = View.GONE

        chatRV.layoutManager = LinearLayoutManager(context)
        chatList = act.chatList.filter { (it.from == act.userkey && it.to == act.chattingWith) || (it.from == act.chattingWith && it.to == act.userkey)}.sortedBy { it.timestamp }.toMutableList()
        chatRV.adapter = ChatAdapter(chatList, context!!, act.userkey, act.photoUrl, act.firebaseCoworkerList.first { it.id == act.chattingWith }.photo, resources)

        sendButton.setOnClickListener {
            val text = chatField.text.toString()
            chatField.setText("")
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            val timestamp = sdf.format(Date())
            val chatItem = ChatItem.create()
            chatItem.id = ""
            chatItem.from = act.userkey
            chatItem.to = act.chattingWith
            chatItem.text = text
            chatItem.timestamp = timestamp
            chatList.add(chatItem)
            act.chatList.add(chatItem)
            createChat(act.userkey, act.chattingWith, text, act.database)
            chatList.sortBy { it.timestamp }
            chatRV.adapter = ChatAdapter(chatList, context!!, act.userkey, act.photoUrl, act.firebaseCoworkerList.first { it.id == act.chattingWith }.photo, resources)
        }
    }
}