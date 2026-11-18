package io.github.amirisback.androidapp.core.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@PublishedApi
internal val sharedGson: Gson = Gson()

fun Any.toJson(): String = sharedGson.toJson(this)

inline fun <reified T> fromJson(json: String?): T? {
    if (json.isNullOrBlank()) return null
    return runCatching {
        sharedGson.fromJson<T>(json, object : TypeToken<T>() {}.type)
    }.getOrNull()
}
