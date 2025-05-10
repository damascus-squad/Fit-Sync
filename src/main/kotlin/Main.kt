package org.damascus

import org.damascus.di.appModule
import org.damascus.presentation.ui.FitSyncApp
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.getKoin

suspend fun main() {
    startKoin {
        modules(appModule)
    }

    val ui: FitSyncApp = getKoin().get()
    ui.start()

}