package com.hato.eskakomon

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Realm初期設定
        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
//        Realm.deleteRealm(config)   // 開発用
        Realm.setDefaultConfiguration(config)
    }
}