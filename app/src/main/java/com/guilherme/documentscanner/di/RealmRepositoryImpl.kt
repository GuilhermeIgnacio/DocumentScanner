package com.guilherme.documentscanner.di

import io.realm.kotlin.Realm

class RealmRepositoryImpl(
    repository: Realm
) : RealmRepository {
    override suspend fun doSomething() {
        println("Hello from D.I")
    }
}