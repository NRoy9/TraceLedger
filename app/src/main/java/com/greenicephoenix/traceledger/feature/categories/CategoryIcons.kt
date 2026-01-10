package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Stable icon registry for categories.
 * Key = persisted string ID
 */
object CategoryIcons {

    val all: Map<String, ImageVector> = mapOf(

        // ---- Food & Daily ----
        "food" to Icons.Filled.Restaurant,
        "groceries" to Icons.Filled.ShoppingCart,
        "coffee" to Icons.Filled.Coffee,
        "bakery" to Icons.Filled.Cake,
        "delivery" to Icons.Filled.DeliveryDining,
        "water" to Icons.Filled.WaterDrop,

        // ---- Transport ----
        "transport" to Icons.Filled.DirectionsCar,
        "car" to Icons.Filled.LocalGasStation,
        "bike" to Icons.Filled.TwoWheeler,
        "bus" to Icons.Filled.DirectionsBus,
        "train" to Icons.Filled.Train,
        "flight" to Icons.Filled.Flight,
        "taxi" to Icons.Filled.LocalTaxi,

        // ---- Housing & Utilities ----
        "home" to Icons.Filled.Home,
        "electricity" to Icons.Filled.Bolt,
        "internet" to Icons.Filled.Wifi,
        "phone" to Icons.Filled.PhoneAndroid,
        "gas" to Icons.Filled.LocalFireDepartment,

        // ---- Shopping & Personal ----
        "shopping" to Icons.Filled.ShoppingBag,
        "clothing" to Icons.Filled.Checkroom,
        "electronics" to Icons.Filled.Devices,
        "beauty" to Icons.Filled.Face,
        "gift" to Icons.Filled.CardGiftcard,

        // ---- Health ----
        "healthcare" to Icons.Filled.MedicalServices,
        "hospital" to Icons.Filled.LocalHospital,
        "pharmacy" to Icons.Filled.Medication,
        "fitness" to Icons.Filled.FitnessCenter,
        "insurance" to Icons.Filled.HealthAndSafety,

        // ---- Entertainment ----
        "entertainment" to Icons.Filled.Movie,
        "music" to Icons.Filled.MusicNote,
        "games" to Icons.Filled.SportsEsports,
        "subscription" to Icons.Filled.Subscriptions,

        // ---- Income / Finance ----
        "salary" to Icons.Filled.AttachMoney,
        "freelance" to Icons.Filled.Work,
        "business" to Icons.Filled.Business,
        "bonus" to Icons.Filled.Star,
        "interest" to Icons.AutoMirrored.Filled.TrendingUp,
        "investment" to Icons.AutoMirrored.Filled.ShowChart,
        "account_credit" to Icons.Filled.AccountBalance,
        "refund" to Icons.Filled.Replay,

        // ---- Misc ----
        "wallet" to Icons.Filled.AccountBalanceWallet,
        "default" to Icons.Default.Category
    )

    val ids: List<String> = all.keys.toList()
}
