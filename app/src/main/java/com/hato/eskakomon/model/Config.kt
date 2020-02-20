package com.hato.eskakomon.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Config (
    @PrimaryKey open var id: String = "",
    open var range: Int = 0,
    open var num: Int = 0,
    open var random: Int = 0
) : RealmObject() {}