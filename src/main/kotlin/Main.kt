package org.damascus

import org.damascus.di.appModule
import org.koin.core.context.GlobalContext.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }
}