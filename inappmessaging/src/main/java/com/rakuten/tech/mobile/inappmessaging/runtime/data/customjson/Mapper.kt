package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal fun interface Mapper<F, T> {
    fun mapFrom(from: F): T
}
