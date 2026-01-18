package com.greenicephoenix.traceledger.core.database.converters

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

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

    // -------- YearMonth --------
    @TypeConverter
    fun fromYearMonth(value: YearMonth?): String? =
        value?.toString() // e.g. 2026-01

    @TypeConverter
    fun toYearMonth(value: String?): YearMonth? =
        value?.let { YearMonth.parse(it) }

}