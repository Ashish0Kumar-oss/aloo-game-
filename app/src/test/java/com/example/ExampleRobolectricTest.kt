package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GamePreferences
import com.example.model.SkinType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aloo Game", appName)
    }

    @Test
    fun `test game preferences defaults and unlocks`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = GamePreferences(context)

        // Starting coins
        assertTrue(prefs.alooCoins >= 50)

        // Classic skin is unlocked by default
        assertTrue(prefs.isSkinUnlocked(SkinType.CLASSIC_CHOTU))

        // Recipes catalog has items
        val recipes = prefs.getRecipes()
        assertTrue(recipes.isNotEmpty())
        assertNotNull(recipes.firstOrNull { it.id == "samosa" })
    }
}
