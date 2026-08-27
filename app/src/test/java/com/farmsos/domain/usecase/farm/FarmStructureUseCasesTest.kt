package com.farmsos.domain.usecase.farm

import com.farmsos.domain.model.Farm
import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.model.Shed
import com.farmsos.testing.FakeFarmRepository
import com.farmsos.testing.FakeFlockRepository
import com.farmsos.testing.FakeShedRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FarmStructureUseCasesTest {

    private lateinit var farms: FakeFarmRepository
    private lateinit var sheds: FakeShedRepository
    private lateinit var flocks: FakeFlockRepository

    private val farm = Farm("farm-1", "North Farm", "Nashik", "user-1", 1, 1)
    private val shed = Shed("shed-1", "farm-1", "Shed A", 1000, "", true, 1, 1)

    @Before
    fun setUp() {
        farms = FakeFarmRepository().also { it.seed(farm) }
        sheds = FakeShedRepository().also { it.seed(shed) }
        flocks = FakeFlockRepository()
    }

    @Test
    fun createFarmRequiresNameAndLocation() = runTest {
        val useCase = CreateFarmUseCase(farms)
        assertTrue(useCase(" ", "Nashik", "user-1").isFailure)
        assertTrue(useCase("North", " ", "user-1").isFailure)
        assertTrue(useCase("North", "Nashik", "user-1").isSuccess)
    }

    @Test
    fun createShedMustBelongToExistingFarm() = runTest {
        val useCase = CreateShedUseCase(farms, sheds)
        assertTrue(useCase("missing", "Shed B", 10, "").isFailure)
        assertTrue(useCase("farm-1", "Shed B", 10, "").isSuccess)
    }

    @Test
    fun createFlockValidatesBirdsDateAndShedFarm() = runTest {
        val useCase = CreateFlockUseCase(sheds, flocks)
        val base = Flock(
            id = "",
            farmId = "farm-1",
            shedId = "shed-1",
            flockCode = "F-1",
            breed = "BV300",
            strain = "A",
            placementDate = "2026-01-01",
            initialBirds = 100,
            currentLiveBirds = 100,
            status = FlockStatus.ACTIVE,
            targetProduction = "eggs",
            notes = "",
            createdAt = 0,
            updatedAt = 0
        )
        assertTrue(useCase(base.copy(initialBirds = 0)).isFailure)
        assertTrue(useCase(base.copy(placementDate = "not-a-date")).isFailure)
        assertTrue(useCase(base.copy(farmId = "other-farm")).isFailure)
        assertTrue(useCase(base.copy(shedId = "missing")).isFailure)
        val created = useCase(base)
        assertTrue(created.isSuccess)
        assertEquals("farm-1", created.getOrThrow().farmId)
        assertEquals("shed-1", created.getOrThrow().shedId)
    }
}
