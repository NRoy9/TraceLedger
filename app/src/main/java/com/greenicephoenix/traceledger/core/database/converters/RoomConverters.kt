package com.greenicephoenix.traceledger.core.database.converters

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class RoomConverters {

    // -------- BigDecimal --------
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? =
        value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? =
        value?.let { BigDecimal(it) }

    // -------- LocalDate --------
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    // -------- Instant --------
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? =
        instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }
}