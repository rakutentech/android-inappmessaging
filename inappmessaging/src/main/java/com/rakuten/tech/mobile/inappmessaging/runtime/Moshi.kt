package com.rakuten.tech.mobile.inappmessaging.runtime

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

inline fun <reified E> Moshi.listAdapter(elementType: Type = E::class.java): JsonAdapter<List<E>> {
    return adapter(listType<E>(elementType))
}

inline fun <reified K, reified V> Moshi.mapAdapter(
    keyType: Type = K::class.java,
    valueType: Type = V::class.java
): JsonAdapter<Map<K, V>> {
    return adapter(mapType<K, V>(keyType, valueType))
}

inline fun <reified E> listType(elementType: Type = E::class.java): Type {
    return Types.newParameterizedType(List::class.java, elementType)
}

inline fun <reified K, reified V> mapType(
    keyType: Type = K::class.java,
    valueType: Type = V::class.java
): Type {
    return Types.newParameterizedType(Map::class.java, keyType, valueType)
}

/**
 * To convert Json response to List of any type just call this method with appropriate class and json data.
 * @param elementType: Class
 * @param data: Json response
 *
 * @return List of data
 */
inline fun <reified E> Moshi.fromJsonToList(elementType: Type = E::class.java, data: String): List<E> {
    return listAdapter<E>(elementType).fromJson(data).orEmpty()
}

/**
 * To convert List of any type to Json response just call this method with appropriate class and list data.
 * @param elementType: Class
 * @param data: List of any type
 *
 * @return Json Data
 */
inline fun <reified E> Moshi.fromListToJson(elementType: Type = E::class.java, data: List<E>?): String {
    return listAdapter<E>(elementType).toJson(data)
}

/**
 * To convert Json response to Any class type.
 * @param elementType: Class
 * @param data: Json response
 */
inline fun <reified E> Moshi.fromJson(elementType: Type = E::class.java, data: String): E? {
    return adapter<E>(elementType).fromJson(data)
}

/**
 * To convert class object to json response.
 * @param elementType: Class
 * @param data: Json response
 */
inline fun <reified E> Moshi.toJson(elementType: Type = E::class.java, data: E?): String {
    return adapter<E>(elementType).toJson(data)
}
