/**
 * Created by Ilia Shelkovenko on 11.07.2020.
 */

package ru.skillbranch.gameofthrones

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object{
        private lateinit var context : Context

        fun applicationContext() : Context{
            return context
        }
    }
}