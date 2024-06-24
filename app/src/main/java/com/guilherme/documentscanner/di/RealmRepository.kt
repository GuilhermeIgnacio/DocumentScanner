package com.guilherme.documentscanner.di

import android.net.Uri
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.flow.Flow

interface RealmRepository {
    suspend fun fetchData(): Flow<List<Document>>
    suspend fun writeObject(uriList: List<String>)
}