package com.greenicephoenix.traceledger.core.importer

/**
 * Simple in-memory holder for [ParsedImportData] passed from
 * ImportExportScreen → ImportMappingScreen via navigation.
 *
 * We cannot put ParsedImportData into savedStateHandle (requires Parcelable
 * or a custom NavType). Since this data lives only for the duration of a
 * single import session and is always recreated by parsing the file,
 * an in-memory singleton is safe and simple.
 *
 * Cleared immediately after the mapping screen reads it, and also cleared
 * when import completes or is cancelled.
 */
object CsvImportHolder {
    var pendingData: ParsedImportData? = null

    fun set(data: ParsedImportData) { pendingData = data }
    fun take(): ParsedImportData?    { val d = pendingData; pendingData = null; return d }
    fun clear()                      { pendingData = null }
}