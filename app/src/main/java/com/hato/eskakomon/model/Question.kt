package com.hato.eskakomon.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Question (
    @PrimaryKey open var id: Long = 0,
    open var year: Int = 0,
    open var number: Int = 0,
    open var sentence: String = "",
    open var choice_a: String = "",
    open var choice_i: String = "",
    open var choice_u: String = "",
    open var choice_e: String = "",
    open var answer: String = "",
    open var image_path: String = "",
    open var miss: Int = 0,
    open var check: Int = 0
) : RealmObject() {}

