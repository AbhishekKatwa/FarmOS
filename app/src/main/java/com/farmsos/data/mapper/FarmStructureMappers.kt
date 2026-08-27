package com.farmsos.data.mapper

import com.farmsos.data.remote.dto.FarmDto
import com.farmsos.data.remote.dto.FlockDto
import com.farmsos.data.remote.dto.ShedDto
import com.farmsos.domain.model.Farm
import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.model.Shed

fun FarmDto.toDomain(): Farm = Farm(
    id = id.orEmpty(),
    name = name,
    location = location,
    ownerId = ownerId,
    createdAt = 0L,
    updatedAt = 0L,
    isActive = isActive
)

fun ShedDto.toDomain(): Shed = Shed(
    id = id.orEmpty(),
    farmId = farmId,
    name = name,
    capacity = capacity,
    notes = notes,
    isActive = isActive,
    createdAt = 0L,
    updatedAt = 0L
)

fun FlockDto.toDomain(): Flock = Flock(
    id = id.orEmpty(),
    farmId = farmId,
    shedId = shedId,
    flockCode = flockCode,
    breed = breed,
    strain = strain,
    placementDate = placementDate.take(10),
    initialBirds = initialBirds,
    currentLiveBirds = currentLiveBirds,
    status = runCatching { FlockStatus.valueOf(status) }.getOrDefault(FlockStatus.PLANNED),
    targetProduction = targetProduction,
    notes = notes,
    createdAt = 0L,
    updatedAt = 0L
)

fun Flock.toInsertDto(): FlockDto = FlockDto(
    farmId = farmId,
    shedId = shedId,
    flockCode = flockCode,
    breed = breed,
    strain = strain,
    placementDate = placementDate,
    initialBirds = initialBirds,
    currentLiveBirds = currentLiveBirds,
    status = status.name,
    targetProduction = targetProduction,
    notes = notes
)
