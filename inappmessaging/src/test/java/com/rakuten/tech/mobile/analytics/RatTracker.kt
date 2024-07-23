package com.rakuten.tech.mobile.analytics

class RatTracker {
    companion object {
        fun event(type: String, parameters: Map<String, Any>): Event = Event(type, parameters)
    }
}