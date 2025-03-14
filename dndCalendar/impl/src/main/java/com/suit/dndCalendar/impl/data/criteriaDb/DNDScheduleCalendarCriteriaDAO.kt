package com.suit.dndCalendar.impl.data.criteriaDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface DNDScheduleCalendarCriteriaDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCriteria(criteriaEntity: DNDScheduleCalendarCriteriaEntity)

    @Transaction
    suspend fun replaceCriteria(criteriaEntity: DNDScheduleCalendarCriteriaEntity) {
        clearTable()
        insertCriteria(criteriaEntity)
    }

    @Query("DELETE FROM DNDScheduleCalendarCriteriaEntity")
    suspend fun clearTable()

    @Query("SELECT * FROM DNDScheduleCalendarCriteriaEntity LIMIT 1")
    fun criteriaFlow(): Flow<DNDScheduleCalendarCriteriaEntity?>
}