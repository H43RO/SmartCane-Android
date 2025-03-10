package com.just_graduate.smartcane

import android.app.Application
import android.content.Context
import com.just_graduate.smartcane.di.repositoryModule
import com.just_graduate.smartcane.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class BaseApplication : Application() {

    init{
        instance = this
    }

    companion object {
        lateinit var instance: BaseApplication
        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            Timber.plant(Timber.DebugTree())
            // 로그를 찍어볼 수 있다.
            // 에러확인 - androidLogger(Level.ERROR)
            androidLogger()
            // Android Context를 넘겨준다.
            androidContext(this@BaseApplication)
            // assets/koin.properties 파일에서 프로퍼티를 가져옴
            androidFileProperties()
            //module list
            modules(listOf(repositoryModule, viewModelModule))
        }

    }

}