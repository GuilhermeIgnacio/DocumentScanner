package com.guilherme.documentscanner.di

import android.net.Uri
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class RealmRepositoryImpl(
    private val realm: Realm
) : RealmRepository {
    override suspend fun fetchData(): Flow<List<Document>> {
        return realm.query<Document>().asFlow().map { it.list }
    }

    override suspend fun writeObject(uriList: List<String>) {

        val list = realmListOf(*uriList.toTypedArray())

        realm.write {
            val document = Document().apply {
                uri = list
            }
            copyToRealm(document)
        }

    }
}