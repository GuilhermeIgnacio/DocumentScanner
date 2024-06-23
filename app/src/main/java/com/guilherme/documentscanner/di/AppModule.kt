package com.guilherme.documentscanner.di

import com.guilherme.documentscanner.presentation.MainViewModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {

    single {
        val config = RealmConfiguration.create(schema = setOf(Document::class))
        Realm.open(config)
    }

    single<RealmRepository> {
        RealmRepositoryImpl(get())
    }

    viewModel {
        MainViewModel(get())
    }

}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}