package com.farmsos.data.repository

import com.farmsos.data.remote.dto.*
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.HealthRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(private val db: Postgrest) : HealthRepository {
    private fun MedicineDto.d() = Medicine(
        id.orEmpty(),
        farmId,
        name,
        manufacturer,
        category,
        unit,
        openingStock,
        openingCost,
        notes
    );

    private fun VaccineDto.d() = Vaccine(id.orEmpty(), farmId, name, manufacturer, notes)
    override suspend fun medicines(farmId: String) = runCatching {
        db["medicines"].select { filter {MedicineDto::farmId eq farmId }}.decodeList<MedicineDto>()
            .map { it.d() }
    }

    override suspend fun vaccines(farmId: String) = runCatching {
        db["vaccines"].select { filter {VaccineDto::farmId eq farmId }}.decodeList<VaccineDto>()
            .map { it.d() }
    }

    override suspend fun reminders(farmId: String) = runCatching {
        db["upcoming_vaccination_reminders"].select { filter {VaccinationReminderDto::farmId eq farmId }}
            .decodeList<VaccinationReminderDto>().map {
            VaccinationReminder(
                it.scheduleId,
                it.farmId,
                it.targetFlockId,
                it.plannedDate,
                it.vaccineName,
                it.dose,
                it.route
            )
        }
    }

    override suspend fun expiryAlerts(farmId: String) = runCatching {
        db["medicine_expiry_alerts"].select { filter {MedicineExpiryAlertDto::farmId eq farmId }}
            .decodeList<MedicineExpiryAlertDto>().map {
            MedicineExpiryAlert(
                it.purchaseId,
                it.farmId,
                it.medicineId,
                it.name,
                it.batch,
                it.expiryDate,
                it.quantity,
                it.unit
            )
        }
    }

    override suspend fun addMedicine(v: Medicine) = runCatching {
        db["medicines"].insert(
            MedicineDto(
                farmId = v.farmId,
                name = v.name,
                manufacturer = v.manufacturer,
                category = v.category,
                unit = v.unit,
                openingStock = v.openingStock,
                openingCost = v.openingCost,
                notes = v.notes
            )
        ) { select() }.decodeSingle<MedicineDto>().d()
    }

    override suspend fun purchaseMedicine(v: MedicinePurchase) = runCatching {
        db["medicine_purchases"].insert(
            MedicinePurchaseDto(
                v.medicineId,
                v.farmId,
                v.supplier,
                v.quantity,
                v.unit,
                v.purchaseCost,
                v.batch,
                v.purchaseDate,
                v.expiryDate,
                v.notes
            )
        ); Unit
    }

    override suspend fun useMedicine(v: MedicineUsage) = runCatching {
        db["medicine_usage"].insert(
            MedicineUsageDto(
                v.medicineId,
                v.farmId,
                v.shedId,
                v.flockId,
                v.date,
                v.quantity,
                v.reason,
                v.notes
            )
        ); Unit
    }

    override suspend fun addVaccine(v: Vaccine) = runCatching {
        db["vaccines"].insert(
            VaccineDto(
                farmId = v.farmId,
                name = v.name,
                manufacturer = v.manufacturer,
                notes = v.notes
            )
        ) { select() }.decodeSingle<VaccineDto>().d()
    }

    override suspend fun saveSchedule(v: VaccinationSchedule) = runCatching {
        db["vaccination_schedules"].insert(
            VaccinationScheduleDto(
                farmId = v.farmId,
                vaccineId = v.vaccineId,
                targetFlockId = v.targetFlockId,
                plannedDate = v.plannedDate,
                dose = v.dose,
                route = v.route,
                notes = v.notes
            )
        ) { select() }.decodeSingle<VaccinationScheduleDto>().let {
            VaccinationSchedule(
                it.id.orEmpty(),
                it.farmId,
                it.vaccineId,
                it.targetFlockId,
                it.plannedDate,
                it.dose,
                it.route,
                it.notes
            )
        }
    }

    override suspend fun recordVaccination(v: VaccinationRecord) = runCatching {
        db["vaccination_records"].insert(
            VaccinationRecordDto(
                v.scheduleId,
                v.vaccineId,
                v.farmId,
                v.targetFlockId,
                v.plannedDate,
                v.actualDate,
                v.dose,
                v.route,
                v.notes
            )
        ); Unit
    }
}
