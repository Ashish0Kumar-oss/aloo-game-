package com.example.model

enum class SkinType {
    CLASSIC_CHOTU,
    GARAM_SPICY,
    SAMOSA_WARRIOR,
    FRENCH_FRY_FREDDY,
    ALOO_TIKKI_KING,
    PARATHA_PRINCE,
    SPACE_SPUD
}

data class AlooSkin(
    val id: SkinType,
    val name: String,
    val hindiTitle: String,
    val description: String,
    val perk: String,
    val price: Int,
    val isUnlocked: Boolean = false,
    val bounceMultiplier: Float = 1.0f,
    val heatResistance: Float = 1.0f,
    val coinMultiplier: Float = 1.0f,
    val baseColorHex: Long = 0xFFF5B041,
    val shadowColorHex: Long = 0xFFD38A28
)

enum class PowerUpType(
    val title: String,
    val hindiTitle: String,
    val durationSec: Float,
    val description: String
) {
    MIRCHI_RUSH("Mirchi Rush", "तीखी मिर्ची", 6.0f, "Fiery speed & 2x combo points!"),
    DHANIYA_FLOAT("Dhaniya Float", "हरा धनिया", 7.0f, "Floaty slow-motion gravity!"),
    MAKHAN_SHIELD("Makhan Shield", "मक्खन शील्ड", 8.0f, "Invincible butter bubble absorbs hits!"),
    WATER_COOL("Cool Splash", "ठंडा पानी", 0.0f, "Instantly cools sizzling pan heat!"),
    CHAAT_MASALA("Chaat Masala", "चाट मसाला", 0.0f, "Screen blast destroys all hazards!")
}

enum class HazardType(
    val title: String,
    val dangerLevel: Int
) {
    KNIFE("Kitchen Knife", 2),
    PEELER("Aloo Peeler", 1),
    OIL_DROP("Boiling Oil Splatter", 1),
    FLAME_JET("Stove Fire", 3)
}

data class RecipeDish(
    val id: String,
    val name: String,
    val hindiName: String,
    val description: String,
    val requiredCoins: Int,
    val cookTimeSec: Int,
    val timesCooked: Int = 0,
    val perkUnlocked: String
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1.0f,
    var size: Float,
    val color: Long,
    var rotation: Float = 0f,
    var rotSpeed: Float = 0f,
    val isLeaf: Boolean = false
)

data class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    var alpha: Float = 1.0f,
    val color: Long = 0xFFFFD600
)
