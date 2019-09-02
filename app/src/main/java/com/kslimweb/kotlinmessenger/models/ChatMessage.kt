package com.kslimweb.kotlinmessenger.models

class ChatMessage(val text: String, val id: String, val fromID: String, val toID: String,
                  val timestamp: Long) {
    constructor(): this ("", "", "" , "" , -1)
}