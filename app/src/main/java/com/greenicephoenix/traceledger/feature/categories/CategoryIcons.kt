package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Stable icon IDs used throughout the app.
 *
 * These IDs are persisted in DB via CategoryEntity.icon.
 * NEVER rename existing IDs after release.
 */
object CategoryIconIds {

    // ──────────── FOOD ─────────────────────────────────────
    const val FOOD = "food"
    const val GROCERIES = "groceries"
    const val COFFEE = "coffee"
    const val DELIVERY = "delivery"
    const val DRINKS = "drinks"

    // ──────────── TRANSPORT ───────────────────────────────
    const val TRANSPORT = "transport"
    const val FUEL = "fuel"
    const val FLIGHT = "flight"
    const val TRAIN = "train"
    const val TAXI = "taxi"
    const val BIKE = "bike"
    const val CAR = "car"

    // ──────────── HOME & UTILITIES ────────────────────────
    const val HOME = "home"
    const val RENT = "rent"
    const val BILLS = "bills"
    const val ELECTRICITY = "electricity"
    const val INTERNET = "internet"
    const val PHONE = "phone"
    const val WATER = "water"

    // ──────────── SHOPPING & LIFESTYLE ────────────────────
    const val SHOPPING = "shopping"
    const val CLOTHING = "clothing"
    const val ELECTRONICS = "electronics"
    const val GIFT = "gift"
    const val PERSONAL_CARE = "personal_care"
    const val BEAUTY = "beauty"

    // ──────────── HEALTH ──────────────────────────────────
    const val HEALTHCARE = "healthcare"
    const val FITNESS = "fitness"
    const val HOSPITAL = "hospital"
    const val PHARMACY = "pharmacy"
    const val INSURANCE = "insurance"

    // ──────────── ENTERTAINMENT ───────────────────────────
    const val ENTERTAINMENT = "entertainment"
    const val MOVIES = "movies"
    const val MUSIC = "music"
    const val GAMES = "games"
    const val SUBSCRIPTION = "subscription"

    // ──────────── EDUCATION & TRAVEL ──────────────────────
    const val EDUCATION = "education"
    const val TRAVEL = "travel"

    // ──────────── PETS & FAMILY ───────────────────────────
    const val PETS = "pets"
    const val FAMILY = "family"

    // ──────────── INCOME ──────────────────────────────────
    const val SALARY = "salary"
    const val FREELANCE = "freelance"
    const val BUSINESS = "business"
    const val BONUS = "bonus"
    const val INTEREST = "interest"
    const val REFUND = "refund"
    const val INVESTMENT = "investment"

    // ──────────── INVESTMENTS ─────────────────────────────
    const val STOCKS = "stocks"
    const val MUTUAL_FUNDS = "mutual_funds"
    const val FD = "fd"
    const val GOLD = "gold"
    const val CRYPTO = "crypto"
    const val REAL_ESTATE = "real_estate"
    const val RETIREMENT = "retirement"
    const val BONDS = "bonds"

    // ──────────── GENERIC ─────────────────────────────────
    const val WALLET = "wallet"
    const val OTHER = "other"
}

/**
 * Stable icon registry.
 *
 * Key = persisted icon ID
 * Value = Compose ImageVector
 */
object CategoryIcons {

    val all: Map<String, ImageVector> = mapOf(

        // ──────────── FOOD ─────────────────────────────────────
        CategoryIconIds.FOOD to Icons.Filled.Restaurant,
        CategoryIconIds.GROCERIES to Icons.Filled.ShoppingCart,
        CategoryIconIds.COFFEE to Icons.Filled.Coffee,
        CategoryIconIds.DELIVERY to Icons.Filled.DeliveryDining,
        CategoryIconIds.DRINKS to Icons.Filled.LocalBar,

        // ──────────── TRANSPORT ───────────────────────────────
        CategoryIconIds.TRANSPORT to Icons.Filled.DirectionsCar,
        CategoryIconIds.FUEL to Icons.Filled.LocalGasStation,
        CategoryIconIds.FLIGHT to Icons.Filled.Flight,
        CategoryIconIds.TRAIN to Icons.Filled.Train,
        CategoryIconIds.TAXI to Icons.Filled.LocalTaxi,
        CategoryIconIds.BIKE to Icons.Filled.TwoWheeler,
        CategoryIconIds.CAR to Icons.Filled.DirectionsCar,

        // ──────────── HOME & UTILITIES ────────────────────────
        CategoryIconIds.HOME to Icons.Filled.Home,
        CategoryIconIds.RENT to Icons.Filled.Apartment,
        CategoryIconIds.BILLS to Icons.AutoMirrored.Filled.ReceiptLong,
        CategoryIconIds.ELECTRICITY to Icons.Filled.Bolt,
        CategoryIconIds.INTERNET to Icons.Filled.Wifi,
        CategoryIconIds.PHONE to Icons.Filled.PhoneAndroid,
        CategoryIconIds.WATER to Icons.Filled.WaterDrop,

        // ──────────── SHOPPING & LIFESTYLE ────────────────────
        CategoryIconIds.SHOPPING to Icons.Filled.ShoppingBag,
        CategoryIconIds.CLOTHING to Icons.Filled.Checkroom,
        CategoryIconIds.ELECTRONICS to Icons.Filled.Devices,
        CategoryIconIds.GIFT to Icons.Filled.CardGiftcard,
        CategoryIconIds.PERSONAL_CARE to Icons.Filled.Spa,
        CategoryIconIds.BEAUTY to Icons.Filled.Face,

        // ──────────── HEALTH ──────────────────────────────────
        CategoryIconIds.HEALTHCARE to Icons.Filled.MedicalServices,
        CategoryIconIds.FITNESS to Icons.Filled.FitnessCenter,
        CategoryIconIds.HOSPITAL to Icons.Filled.LocalHospital,
        CategoryIconIds.PHARMACY to Icons.Filled.Medication,
        CategoryIconIds.INSURANCE to Icons.Filled.HealthAndSafety,

        // ──────────── ENTERTAINMENT ───────────────────────────
        CategoryIconIds.ENTERTAINMENT to Icons.Filled.Movie,
        CategoryIconIds.MOVIES to Icons.Filled.Movie,
        CategoryIconIds.MUSIC to Icons.Filled.MusicNote,
        CategoryIconIds.GAMES to Icons.Filled.SportsEsports,
        CategoryIconIds.SUBSCRIPTION to Icons.Filled.Subscriptions,

        // ──────────── EDUCATION & TRAVEL ──────────────────────
        CategoryIconIds.EDUCATION to Icons.Filled.School,
        CategoryIconIds.TRAVEL to Icons.Filled.Luggage,

        // ──────────── PETS & FAMILY ───────────────────────────
        CategoryIconIds.PETS to Icons.Filled.Pets,
        CategoryIconIds.FAMILY to Icons.Filled.FamilyRestroom,

        // ──────────── INCOME ──────────────────────────────────
        CategoryIconIds.SALARY to Icons.Filled.AttachMoney,
        CategoryIconIds.FREELANCE to Icons.Filled.Work,
        CategoryIconIds.BUSINESS to Icons.Filled.Business,
        CategoryIconIds.BONUS to Icons.Filled.Payments,
        CategoryIconIds.INTEREST to Icons.AutoMirrored.Filled.TrendingUp,
        CategoryIconIds.REFUND to Icons.Filled.Replay,
        CategoryIconIds.INVESTMENT to Icons.AutoMirrored.Filled.ShowChart,

        // ──────────── INVESTMENTS ─────────────────────────────
        CategoryIconIds.STOCKS to Icons.Filled.CandlestickChart,
        CategoryIconIds.MUTUAL_FUNDS to Icons.Filled.PieChart,
        CategoryIconIds.FD to Icons.Filled.AccountBalance,
        CategoryIconIds.GOLD to Icons.Filled.MonetizationOn,
        CategoryIconIds.CRYPTO to Icons.Filled.CurrencyBitcoin,
        CategoryIconIds.REAL_ESTATE to Icons.Filled.HomeWork,
        CategoryIconIds.RETIREMENT to Icons.Filled.Savings,
        CategoryIconIds.BONDS to Icons.Filled.RequestQuote,

        // ──────────── GENERIC ─────────────────────────────────
        CategoryIconIds.WALLET to Icons.Filled.AccountBalanceWallet,
        CategoryIconIds.OTHER to Icons.Default.Category,
    )

    /**
     * Ordered IDs for icon picker UI.
     */
    val ids: List<String> = all.keys.toList()

    /**
     * Safe lookup helper.
     */
    fun iconFor(id: String): ImageVector {
        return all[id] ?: Icons.Default.Category
    }
}