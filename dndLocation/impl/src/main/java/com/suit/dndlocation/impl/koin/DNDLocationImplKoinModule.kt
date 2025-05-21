package com.suit.dndlocation.impl.koin

import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.GeocodingManager
import com.suit.dndlocation.impl.DNDLocationRepositoryImpl
import com.suit.dndlocation.impl.GeocodingManagerImpl
import com.suit.dndlocation.impl.db.SavedLocationsDb
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dndLocationImplModule = module {
    single<GeocodingManager> {
        GeocodingManagerImpl(
            engine = CIO.create()
        )
    }
    single { CoroutineScope(Dispatchers.IO) }
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
    single {
        Room.databaseBuilder(
            context = androidContext(),
            SavedLocationsDb::class.java,
            "saved-locations"
        )
            .build()
    }
    single<DNDLocationRepository> {
        DNDLocationRepositoryImpl(
            context = androidContext(),
            savedLocationsDb = get(),
            geocodingManager = get()
        )
    }
}