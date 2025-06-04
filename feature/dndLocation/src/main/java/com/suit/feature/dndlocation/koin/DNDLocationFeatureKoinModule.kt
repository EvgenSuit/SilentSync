package com.suit.feature.dndlocation.koin

import com.suit.dndlocation.impl.koin.dndLocationImplModule
import com.suit.feature.dndlocation.presentation.DNDLocationViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dndLocationFeatureModule = module {
    includes(dndLocationImplModule)

    factory {
        Dispatchers.IO
    }
    factoryOf(::DNDLocationViewModel)
}