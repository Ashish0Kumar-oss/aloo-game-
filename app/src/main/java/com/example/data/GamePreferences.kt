package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AlooSkin
import com.example.model.RecipeDish
import com.example.model.SkinType

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aloo_game_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BOUNCE_HIGH_SCORE = "bounce_high_score"
        private const val KEY_DASH_HIGH_SCORE = "dash_high_score"
        private const val KEY_TOTAL_BOUNCES = "total_bounces"
        private const val KEY_ALOO_COINS = "aloo_coins"
        private const val KEY_SELECTED_SKIN = "selected_skin"
        private const val KEY_UNLOCKED_SKINS = "unlocked_skins"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val PREFIX_RECIPE_COUNT = "recipe_count_"
    }

    var bounceHighScore: Int
        get() = prefs.getInt(KEY_BOUNCE_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_BOUNCE_HIGH_SCORE, value).apply()

    var dashHighScore: Int
        get() = prefs.getInt(KEY_DASH_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_DASH_HIGH_SCORE, value).apply()

    var totalBounces: Int
        get() = prefs.getInt(KEY_TOTAL_BOUNCES, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_BOUNCES, value).apply()

    var alooCoins: Int
        get() = prefs.getInt(KEY_ALOO_COINS, 50) // 50 starter coins!
        set(value) = prefs.edit().putInt(KEY_ALOO_COINS, value).apply()

    var selectedSkin: SkinType
        get() {
            val name = prefs.getString(KEY_SELECTED_SKIN, SkinType.CLASSIC_CHOTU.name) ?: SkinType.CLASSIC_CHOTU.name
            return try {
                SkinType.valueOf(name)
            } catch (e: Exception) {
                SkinType.CLASSIC_CHOTU
            }
        }
        set(value) = prefs.edit().putString(KEY_SELECTED_SKIN, value.name).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    fun isSkinUnlocked(skinType: SkinType): Boolean {
        if (skinType == SkinType.CLASSIC_CHOTU) return true
        val set = prefs.getStringSet(KEY_UNLOCKED_SKINS, emptySet()) ?: emptySet()
        return set.contains(skinType.name)
    }

    fun unlockSkin(skinType: SkinType) {
        val current = (prefs.getStringSet(KEY_UNLOCKED_SKINS, emptySet()) ?: emptySet()).toMutableSet()
        current.add(skinType.name)
        prefs.edit().putStringSet(KEY_UNLOCKED_SKINS, current).apply()
    }

    fun getRecipeCookedCount(recipeId: String): Int {
        return prefs.getInt(PREFIX_RECIPE_COUNT + recipeId, 0)
    }

    fun incrementRecipeCooked(recipeId: String) {
        val current = getRecipeCookedCount(recipeId)
        prefs.edit().putInt(PREFIX_RECIPE_COUNT + recipeId, current + 1).apply()
    }

    fun getSkins(): List<AlooSkin> {
        return listOf(
            AlooSkin(
                id = SkinType.CLASSIC_CHOTU,
                name = "Chotu Aloo",
                hindiTitle = "छोटू आलू",
                description = "The cheerful spud who started it all! Classic, bouncy, and beloved.",
                perk = "Standard balanced bounce & heat resistance",
                price = 0,
                isUnlocked = true,
                bounceMultiplier = 1.0f,
                heatResistance = 1.0f,
                coinMultiplier = 1.0f,
                baseColorHex = 0xFFF5B041,
                shadowColorHex = 0xFFD38A28
            ),
            AlooSkin(
                id = SkinType.GARAM_SPICY,
                name = "Garam Aloo",
                hindiTitle = "गरम मसाला आलू",
                description = "Sporting a flaming red martial band! Born directly on the frying tawa.",
                perk = "+25% Heat resistance & 1.5x Mirchi boost",
                price = 100,
                isUnlocked = isSkinUnlocked(SkinType.GARAM_SPICY),
                bounceMultiplier = 1.1f,
                heatResistance = 1.25f,
                coinMultiplier = 1.2f,
                baseColorHex = 0xFFFF7043,
                shadowColorHex = 0xFFD84315
            ),
            AlooSkin(
                id = SkinType.SAMOSA_WARRIOR,
                name = "Samosa Bro",
                hindiTitle = "समोसा सूरमा",
                description = "Crispy triangular warrior filled with spicy peas, cumin, and determination.",
                perk = "Built-in crispy crust: Starts with +1 Hit Shield",
                price = 220,
                isUnlocked = isSkinUnlocked(SkinType.SAMOSA_WARRIOR),
                bounceMultiplier = 1.05f,
                heatResistance = 1.15f,
                coinMultiplier = 1.3f,
                baseColorHex = 0xFFE0A96D,
                shadowColorHex = 0xFFB87333
            ),
            AlooSkin(
                id = SkinType.FRENCH_FRY_FREDDY,
                name = "French Fry Freddy",
                hindiTitle = "कुरकुरा फ्राइज़",
                description = "Golden crispy potato sticks ready for party time with ketchup!",
                perk = "Super springy: +20% Bounce height",
                price = 350,
                isUnlocked = isSkinUnlocked(SkinType.FRENCH_FRY_FREDDY),
                bounceMultiplier = 1.2f,
                heatResistance = 0.95f,
                coinMultiplier = 1.4f,
                baseColorHex = 0xFFFFD54F,
                shadowColorHex = 0xFFFFB300
            ),
            AlooSkin(
                id = SkinType.ALOO_TIKKI_KING,
                name = "Tikki Badshah",
                hindiTitle = "आलू टिक्की बादशाह",
                description = "Pan-seared golden patty crowned with mint chutney and pomegranate.",
                perk = "+30% Bonus Aloo coins earned per game",
                price = 500,
                isUnlocked = isSkinUnlocked(SkinType.ALOO_TIKKI_KING),
                bounceMultiplier = 1.0f,
                heatResistance = 1.3f,
                coinMultiplier = 1.6f,
                baseColorHex = 0xFFD7CCC8,
                shadowColorHex = 0xFF8D6E63
            ),
            AlooSkin(
                id = SkinType.PARATHA_PRINCE,
                name = "Aloo Paratha King",
                hindiTitle = "पराठा महाराज",
                description = "Richly spiced dough pocket stuffed with spiced potato and royal melting butter.",
                perk = "Butter Magnet: Automatically pulls nearby coins & spices",
                price = 750,
                isUnlocked = isSkinUnlocked(SkinType.PARATHA_PRINCE),
                bounceMultiplier = 1.15f,
                heatResistance = 1.4f,
                coinMultiplier = 1.8f,
                baseColorHex = 0xFFFFE082,
                shadowColorHex = 0xFFFFCA28
            ),
            AlooSkin(
                id = SkinType.SPACE_SPUD,
                name = "Cosmic Spud",
                hindiTitle = "अंतरिक्ष आलू",
                description = "First potato in low Earth orbit. Low gravity booster pack installed!",
                perk = "Anti-gravity float + 2.0x Ultimate coin bonus",
                price = 1200,
                isUnlocked = isSkinUnlocked(SkinType.SPACE_SPUD),
                bounceMultiplier = 1.3f,
                heatResistance = 1.5f,
                coinMultiplier = 2.0f,
                baseColorHex = 0xFF80DEEA,
                shadowColorHex = 0xFF00ACC1
            )
        )
    }

    fun getRecipes(): List<RecipeDish> {
        return listOf(
            RecipeDish(
                id = "french_fries",
                name = "Crispy French Fries",
                hindiName = "कुरकुरे आलू फ्राइज़",
                description = "Golden potato batons salted and tossed in spicy peri-peri masala.",
                requiredCoins = 40,
                cookTimeSec = 3,
                timesCooked = getRecipeCookedCount("french_fries"),
                perkUnlocked = "+5% permanent bounce boost"
            ),
            RecipeDish(
                id = "samosa",
                name = "Punjabi Aloo Samosa",
                hindiName = "पंजाबी आलू समोसा",
                description = "Flaky pyramid crust stuffed with fragrant potato, ginger, and green chillies.",
                requiredCoins = 75,
                cookTimeSec = 4,
                timesCooked = getRecipeCookedCount("samosa"),
                perkUnlocked = "+10% starting heat safety margin"
            ),
            RecipeDish(
                id = "aloo_chaat",
                name = "Chandni Chowk Aloo Chaat",
                hindiName = "चांदनी चौक आलू चाट",
                description = "Crispy fried cubed potatoes drenched in sweet tamarind & spicy mint chutney.",
                requiredCoins = 120,
                cookTimeSec = 4,
                timesCooked = getRecipeCookedCount("aloo_chaat"),
                perkUnlocked = "+15% extra spice duration"
            ),
            RecipeDish(
                id = "aloo_paratha",
                name = "Makkhan Aloo Paratha",
                hindiName = "मक्खन आलू पराठा",
                description = "Whole wheat flatbread stuffed with mashed seasoned aloo and a slab of white butter.",
                requiredCoins = 180,
                cookTimeSec = 5,
                timesCooked = getRecipeCookedCount("aloo_paratha"),
                perkUnlocked = "+20% coin drop rate"
            ),
            RecipeDish(
                id = "dum_aloo",
                name = "Shahi Dum Aloo",
                hindiName = "शाही दम आलू",
                description = "Baby potatoes slow-cooked in aromatic fennel and curd gravy.",
                requiredCoins = 260,
                cookTimeSec = 6,
                timesCooked = getRecipeCookedCount("dum_aloo"),
                perkUnlocked = "+25% high score multiplier"
            )
        )
    }
}
