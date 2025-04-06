package com.suit.feature.dndlocation.koin

import com.suit.dndlocation.impl.koin.dndLocationImplModule
import com.suit.feature.dndlocation.presentation.DNDLocationViewModel
import org.koin.dsl.module

val dndLocationFeatureModule = module {
    includes(dndLocationImplModule)

    single {
        DNDLocationViewModel(
            dndLocationRepository = get()
        )
    }
}