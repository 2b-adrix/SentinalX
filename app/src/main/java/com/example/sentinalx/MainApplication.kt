package com.example.sentinalx

import android.app.Application

class MainApplication : Application() {
    companion object {
        lateinit var instance: MainApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ThreatRepository.init(this)
    }
}
