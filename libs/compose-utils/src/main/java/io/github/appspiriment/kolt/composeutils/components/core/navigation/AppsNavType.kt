package io.github.appspiriment.kolt.composeutils.components.core.navigation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import androidx.savedstate.SavedState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun <reified T> genericNavType(
    isNullableAllowd: Boolean = false
) = object : NavType<T>(isNullableAllowed = isNullableAllowd){
    override fun get(bundle: SavedState, key: String): T? {
        return  bundle.getString(key)?.toValue()
    }

    override fun parseValue(value: String): T {
        return value.toValue()
    }

    override fun put(bundle: SavedState, key: String, value: T) {
       bundle.putString(key, value.encodeToString())
    }

    override fun serializeAsValue(value: T): String {
        return value.encodeToString().let(Uri::encode)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is NavType<*>) return false
        if(other::class.java != this::class.java) return false
        if(isNullableAllowd != other.isNullableAllowed) return false
        return true
    }

    private fun String.toValue(): T = Json.decodeFromString(this)
    private fun T.encodeToString(): String = Json.encodeToString(this)
}

inline fun <reified T> SavedStateHandle.toValue(key:String): T = Json.decodeFromString(
    requireNotNull(get(key)){
        "The value for $key in SavedStateHandle should not be null"
    }
)

inline fun <reified T> typeMapOf(): Pair<KType, NavType<T>>{
    val type = typeOf<T>()
    return type to genericNavType<T>(isNullableAllowd = type.isMarkedNullable)
}