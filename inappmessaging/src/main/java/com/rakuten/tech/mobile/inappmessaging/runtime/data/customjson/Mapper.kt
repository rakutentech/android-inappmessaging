package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

internal interface Mapper<F, T> {
    fun mapFrom(from: F): T
}
