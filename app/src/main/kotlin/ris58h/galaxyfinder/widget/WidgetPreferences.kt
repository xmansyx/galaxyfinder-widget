package ris58h.galaxyfinder.widget

import android.content.Context
import android.content.SharedPreferences

class WidgetPreferences(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Background color methods
    fun getBackgroundColor(appWidgetId: Int): Int {
        return prefs.getInt(
            getBackgroundColorPrefKey(appWidgetId), 
            BG_COLOR_TRANSPARENT // Default transparent background
        )
    }
    
    fun saveBackgroundColor(appWidgetId: Int, colorChoice: Int) {
        prefs.edit().putInt(getBackgroundColorPrefKey(appWidgetId), colorChoice).apply()
    }
    
    // Text color methods
    fun getTextColor(appWidgetId: Int): Int {
        return prefs.getInt(
            getTextColorPrefKey(appWidgetId), 
            TEXT_COLOR_LIGHT // Default light text
        )
    }
    
    fun saveTextColor(appWidgetId: Int, colorChoice: Int) {
        prefs.edit().putInt(getTextColorPrefKey(appWidgetId), colorChoice).apply()
    }
    
    // Custom color methods
    fun getCustomBackgroundColor(appWidgetId: Int): Int {
        return prefs.getInt(
            getCustomBackgroundColorPrefKey(appWidgetId),
            DEFAULT_CUSTOM_BG_COLOR
        )
    }
    
    fun saveCustomBackgroundColor(appWidgetId: Int, color: Int) {
        prefs.edit().putInt(getCustomBackgroundColorPrefKey(appWidgetId), color).apply()
    }
    
    fun getCustomTextColor(appWidgetId: Int): Int {
        return prefs.getInt(
            getCustomTextColorPrefKey(appWidgetId),
            DEFAULT_CUSTOM_TEXT_COLOR
        )
    }
    
    fun saveCustomTextColor(appWidgetId: Int, color: Int) {
        prefs.edit().putInt(getCustomTextColorPrefKey(appWidgetId), color).apply()
    }
    
    // Transparency settings
    fun getBackgroundTransparency(appWidgetId: Int): Int {
        return prefs.getInt(
            getBackgroundTransparencyPrefKey(appWidgetId),
            DEFAULT_TRANSPARENCY
        )
    }
    
    fun saveBackgroundTransparency(appWidgetId: Int, transparencyValue: Int) {
        prefs.edit().putInt(getBackgroundTransparencyPrefKey(appWidgetId), transparencyValue).apply()
    }
    
    // Delete all preferences
    fun deletePrefs(appWidgetId: Int) {
        prefs.edit()
            .remove(getBackgroundColorPrefKey(appWidgetId))
            .remove(getTextColorPrefKey(appWidgetId))
            .remove(getCustomBackgroundColorPrefKey(appWidgetId))
            .remove(getCustomTextColorPrefKey(appWidgetId))
            .remove(getBackgroundTransparencyPrefKey(appWidgetId))
            .apply()
    }
    
    // Preference key getters
    private fun getBackgroundColorPrefKey(appWidgetId: Int): String {
        return "$PREFS_PREFIX$appWidgetId$BG_COLOR_KEY"
    }
    
    private fun getTextColorPrefKey(appWidgetId: Int): String {
        return "$PREFS_PREFIX$appWidgetId$TEXT_COLOR_KEY"
    }
    
    private fun getCustomBackgroundColorPrefKey(appWidgetId: Int): String {
        return "$PREFS_PREFIX$appWidgetId$CUSTOM_BG_COLOR_KEY"
    }
    
    private fun getCustomTextColorPrefKey(appWidgetId: Int): String {
        return "$PREFS_PREFIX$appWidgetId$CUSTOM_TEXT_COLOR_KEY"
    }
    
    private fun getBackgroundTransparencyPrefKey(appWidgetId: Int): String {
        return "$PREFS_PREFIX$appWidgetId$BG_TRANSPARENCY_KEY"
    }
    
    companion object {
        private const val PREFS_NAME = "ris58h.galaxyfinder.widget.WidgetPrefs"
        private const val PREFS_PREFIX = "appwidget_"
        private const val BG_COLOR_KEY = "_background_color"
        private const val TEXT_COLOR_KEY = "_text_color"
        private const val CUSTOM_BG_COLOR_KEY = "_custom_bg_color"
        private const val CUSTOM_TEXT_COLOR_KEY = "_custom_text_color"
        private const val BG_TRANSPARENCY_KEY = "_bg_transparency"
        
        // Background color choices
        const val BG_COLOR_TRANSPARENT = 0
        const val BG_COLOR_DARK = 1
        const val BG_COLOR_LIGHT = 2
        const val BG_COLOR_CUSTOM = 3
        
        // Text color choices
        const val TEXT_COLOR_LIGHT = 0
        const val TEXT_COLOR_DARK = 1
        const val TEXT_COLOR_CUSTOM = 2
        
        // Default custom colors
        const val DEFAULT_CUSTOM_BG_COLOR = 0xFF2196F3.toInt() // Material Blue
        const val DEFAULT_CUSTOM_TEXT_COLOR = 0xFFFFFFFF.toInt() // White
        const val DEFAULT_TRANSPARENCY = 128 // Half transparent (0-255)
    }
} 
