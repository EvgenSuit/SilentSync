package com.suit.feature.dndlocation

import app.cash.turbine.test
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.GeocodingResult
import com.suit.feature.dndlocation.presentation.DNDLocationViewModel
import com.suit.feature.dndlocation.presentation.ui.DNDLocationIntent
import com.suit.testutil.test.MainDispatcherRule
import com.suit.testutil.test.TestException
import com.suit.utility.analytics.SilentSyncAnalytics
import com.suit.utility.ui.DNDLocationUIEvent
import com.suit.utility.ui.UIText
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DNDLocationViewModelTest {
    private lateinit var viewModel: DNDLocationViewModel

    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val dndLocationRepository = mockk<DNDLocationRepository>(relaxed = true) {
        coEvery { isFeatureEnabled() } returns flowOf(true)
    }
    private val analytics = mockk<SilentSyncAnalytics>(relaxed = true)
    private val exception = TestException()

    private fun setup() {
        viewModel = DNDLocationViewModel(
            dndLocationRepository = dndLocationRepository,
            dispatcher = mainDispatcherRule.dispatcher,
            analytics = analytics
        )
    }

    @Before
    fun before() {
        setup()
    }

    @Test
    fun `record exception on service toggle error`() = runTest {
        coEvery { dndLocationRepository.toggleLocationService(any(), any()) } throws exception

        viewModel.apply {
            launch {
                uiEvent.test {
                    assertEquals(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.unknown_error)), awaitItem())
                }
            }
            handleIntent(DNDLocationIntent.ToggleService(true))
            advanceUntilIdle()
        }
        verify { analytics.recordException(exception) }
    }

    @Test
    fun `emit error and record exception on feature availability toggle error`() = runTest {
        coEvery { dndLocationRepository.toggleFeatureAvailability(any()) } throws exception

        launch {
            viewModel.uiEvent.test {
                assertEquals(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_toggle_feature_availability)), awaitItem())
            }
        }

        viewModel.handleIntent(DNDLocationIntent.ToggleLocationFeatureAvailability(true))
        advanceUntilIdle()
        verify { analytics.recordException(exception) }
    }

    @Test
    fun `ui state updated on location input`() = runTest {
        viewModel.apply {
            handleIntent(DNDLocationIntent.LocationInput("test;"))
            assertEquals("test", uiState.value.locationInput)
            handleIntent(DNDLocationIntent.LocationInput("t".repeat(260)))
            assertEquals("t".repeat(256), uiState.value.locationInput)
        }
    }

    @Test
    fun `successful geocoding result`() = runTest {
        val geocodingResult = mockk<GeocodingResult>()
        coEvery { dndLocationRepository.geocode(any()) } returns geocodingResult

        viewModel.apply {
            handleIntent(DNDLocationIntent.LocationInput("test;"))
            advanceUntilIdle()
            assertEquals(geocodingResult, uiState.value.geocodingResult)
        }
    }
    @Test
    fun `unsuccessful geocoding result`() = runTest {
        coEvery { dndLocationRepository.geocode(any()) } throws exception

        viewModel.apply {
            launch {
                viewModel.uiEvent.test {
                    assertEquals(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_fetch_locations)), awaitItem())
                }
            }
            handleIntent(DNDLocationIntent.LocationInput("test;"))
            advanceUntilIdle()
            assertEquals(null, uiState.value.geocodingResult)
        }
    }

    @Test
    fun `unsuccessful location deletion`() = runTest {
        coEvery { dndLocationRepository.deleteLocation(any()) } throws exception

        viewModel.apply {
            launch {
                uiEvent.test {
                    assertEquals(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_delete_location)), awaitItem())
                }
            }
            viewModel.handleIntent(DNDLocationIntent.DeleteLocation(0))
            advanceUntilIdle()
        }
        verify { analytics.recordException(exception) }
    }

    @Test
    fun `unsuccessful location confirmation`() = runTest {
        coEvery { dndLocationRepository.confirmLocation(any(), any(), any(), any()) } throws exception

        viewModel.apply {
            launch {
                uiEvent.test {
                    assertEquals(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_add_location)), awaitItem())
                }
            }
            viewModel.handleIntent(DNDLocationIntent.ConfirmLocation(mockk(), true, true, mockk()))
            advanceUntilIdle()
        }
        verify { analytics.recordException(exception) }
    }
}