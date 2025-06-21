package com.example.coneonapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferenceHelper private constructor(context: Context) {

    companion object {
        private const val PREF_NAME = "MyAppPreferences"
        @Volatile private var instance: SharedPreferenceHelper? = null

        fun getInstance(context: Context): SharedPreferenceHelper {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferenceHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun putValues(builder: SharedPreferences.Editor.() -> Unit) {
        sharedPreferences.edit().apply {
            builder()
            apply()
        }
    }

    fun putString(key: String, value: String) = putValues {
        putString(key, value)
    }

    fun putInt(key: String, value: Int) = putValues {
        putInt(key, value)
    }

    fun putBoolean(key: String, value: Boolean) = putValues {
        putBoolean(key, value)
    }

    fun <T> putArrayList(key: String, list: ArrayList<T>) {
        val json = Gson().toJson(list)
        putString(key, json)
    }

    inline fun <reified T> getArrayList(key: String): ArrayList<T> {
        val json = getString(key, null)
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<ArrayList<T>>() {}.type
            Gson().fromJson(json, type)
        } else {
            arrayListOf()
        }
    }

    fun <T> putList(key: String, list: List<T>) {
        val json = Gson().toJson(list)
        putString(key, json)
    }

    inline fun <reified T> getList(key: String): List<T> {
        val json = getString(key, null)
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<T>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun remove(key: String) = putValues {
        remove(key)
    }

    fun clear() = putValues {
        clear()
    }

    fun getString(key: String, default: String? = null): String? =
        sharedPreferences.getString(key, default)

    fun getInt(key: String, default: Int = 0): Int =
        sharedPreferences.getInt(key, default)

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        sharedPreferences.getBoolean(key, default)
}