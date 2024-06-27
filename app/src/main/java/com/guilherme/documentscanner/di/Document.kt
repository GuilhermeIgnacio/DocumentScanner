package com.guilherme.documentscanner.di

import android.net.Uri
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Document(): RealmObject {

    @PrimaryKey var _id = ObjectId()
    var name: String = ""
    var uri: RealmList<String> = realmListOf()

}