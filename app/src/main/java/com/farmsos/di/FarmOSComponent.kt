package com.farmsos.di

import com.farmsos.FarmOSApplication
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [FarmOSModule::class])
abstract class FarmOSComponent {
    abstract fun inject(application: FarmOSApplication)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: FarmOSApplication): FarmOSComponent
    }

    companion object {
        fun factory(): Factory = DaggerFarmOSComponent.factory()
    }
}
