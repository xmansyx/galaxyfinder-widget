package ris58h.galaxyfinder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews

class FinderWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the widget is deleted, delete the preferences associated with it
        val prefs = WidgetPreferences(context)
        for (appWidgetId in appWidgetIds) {
            prefs.deletePrefs(appWidgetId)
        }
    }
    
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle Samsung-specific reconfiguration intents
        if (intent.action == "com.sec.android.widgetapp.APPWIDGET_RECONFIGURE") {
            // Extract the widget ID if present in the intent extras
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            
            // Launch the configuration activity directly
            val configIntent = Intent(context, ConfigurationActivity::class.java)
            configIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            // Add the widget ID to the intent
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            
            context.startActivity(configIntent)
            return
        }
        
        // Handle other actions
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, FinderWidget::class.java))
            
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget)
    
    // Apply color preferences
    val prefs = WidgetPreferences(context)
    val bgColorChoice = prefs.getBackgroundColor(appWidgetId)
    val textColorChoice = prefs.getTextColor(appWidgetId)
    val transparency = prefs.getBackgroundTransparency(appWidgetId)
    
    // Set background based on color choice, applying transparency to all types
    when (bgColorChoice) {
        WidgetPreferences.BG_COLOR_DARK -> {
            // Get the dark color and apply transparency
            context.getSharedPreferences("widget_custom_bg_prefs", Context.MODE_PRIVATE).edit()
                .putInt("widget_custom_bg_color_$appWidgetId", getColorCompat(context, R.color.widget_background_dark_color))
                .putInt("widget_custom_bg_transparency_$appWidgetId", transparency)
                .apply()
                
            // Use our drawable with transparency
            views.setInt(R.id.widget_input, "setBackgroundResource", R.drawable.widget_background_transparent)
            
            // Apply the dark color with transparency as a fallback
            val darkColorWithAlpha = applyAlpha(getColorCompat(context, R.color.widget_background_dark_color), transparency)
            views.setInt(R.id.widget_input, "setBackgroundColor", darkColorWithAlpha)
        }
        WidgetPreferences.BG_COLOR_LIGHT -> {
            // Get the light color and apply transparency
            context.getSharedPreferences("widget_custom_bg_prefs", Context.MODE_PRIVATE).edit()
                .putInt("widget_custom_bg_color_$appWidgetId", getColorCompat(context, R.color.widget_background_light_color))
                .putInt("widget_custom_bg_transparency_$appWidgetId", transparency)
                .apply()
                
            // Use our drawable with transparency
            views.setInt(R.id.widget_input, "setBackgroundResource", R.drawable.widget_background_transparent)
            
            // Apply the light color with transparency as a fallback
            val lightColorWithAlpha = applyAlpha(getColorCompat(context, R.color.widget_background_light_color), transparency)
            views.setInt(R.id.widget_input, "setBackgroundColor", lightColorWithAlpha)
        }
        WidgetPreferences.BG_COLOR_CUSTOM -> {
            // For custom color, we need to create a background drawable at runtime
            // But RemoteViews doesn't support custom drawables directly
            // So we'll create a temporary drawable resource
            val customBgColor = prefs.getCustomBackgroundColor(appWidgetId)
            
            // We'll set an ID in SharedPreferences that the CustomBackgroundReceiver will use
            context.getSharedPreferences("widget_custom_bg_prefs", Context.MODE_PRIVATE).edit()
                .putInt("widget_custom_bg_color_$appWidgetId", customBgColor)
                .putInt("widget_custom_bg_transparency_$appWidgetId", transparency)
                .apply()
                
            // Use a generic background resource that will be updated on the next refresh
            views.setInt(R.id.widget_input, "setBackgroundResource", R.drawable.widget_background_transparent)
            
            // Apply a custom background color with transparency
            val customColorWithAlpha = applyAlpha(customBgColor, transparency)
            views.setInt(R.id.widget_input, "setBackgroundColor", customColorWithAlpha)
        }
        else -> {
            // For transparent, apply just the transparency level
            context.getSharedPreferences("widget_custom_bg_prefs", Context.MODE_PRIVATE).edit()
                .putInt("widget_custom_bg_color_$appWidgetId", 0x00FFFFFF) // Transparent white
                .putInt("widget_custom_bg_transparency_$appWidgetId", transparency)
                .apply()
                
            // Default to transparent with the specified alpha level
            views.setInt(R.id.widget_input, "setBackgroundResource", R.drawable.widget_background_transparent)
            
            // Apply transparency to a transparent color
            val transparentWithAlpha = applyAlpha(0x00FFFFFF, transparency)
            views.setInt(R.id.widget_input, "setBackgroundColor", transparentWithAlpha)
        }
    }
    
    // Apply text color
    when (textColorChoice) {
        WidgetPreferences.TEXT_COLOR_DARK -> {
            val textColor = getColorCompat(context, R.color.widget_text_dark)
            views.setTextColor(R.id.widget_input, textColor)
            views.setInt(R.id.widget_input, "setHintTextColor", textColor)
        }
        WidgetPreferences.TEXT_COLOR_LIGHT -> {
            val textColor = getColorCompat(context, R.color.widget_text_light)
            views.setTextColor(R.id.widget_input, textColor)
            views.setInt(R.id.widget_input, "setHintTextColor", textColor)
        }
        WidgetPreferences.TEXT_COLOR_CUSTOM -> {
            // Use custom text color
            val customTextColor = prefs.getCustomTextColor(appWidgetId)
            views.setTextColor(R.id.widget_input, customTextColor)
            views.setInt(R.id.widget_input, "setHintTextColor", customTextColor)
        }
        else -> {
            // Default to light text
            val textColor = getColorCompat(context, R.color.widget_text_light)
            views.setTextColor(R.id.widget_input, textColor)
            views.setInt(R.id.widget_input, "setHintTextColor", textColor)
        }
    }

    // Set up the search intent (normal click)
    val launchIntent: Intent? =
        if (Build.VERSION.SDK_INT >= 34) {
            Intent().setClassName("com.sec.android.app.launcher", "com.sec.android.app.launcher.search.SearchActivity")
        } else {
            context.packageManager.getLaunchIntentForPackage("com.samsung.android.app.galaxyfinder")
        }
    val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)
    views.setOnClickPendingIntent(R.id.widget_input, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

// Helper function to apply alpha to a color
private fun applyAlpha(color: Int, alpha: Int): Int {
    val alphaByte = alpha and 0xFF
    val red = android.graphics.Color.red(color)
    val green = android.graphics.Color.green(color)
    val blue = android.graphics.Color.blue(color)
    return android.graphics.Color.argb(alphaByte, red, green, blue)
}

// Helper method to get color in a backward-compatible way
internal fun getColorCompat(context: Context, colorResId: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.resources.getColor(colorResId, context.theme)
    } else {
        @Suppress("DEPRECATION")
        context.resources.getColor(colorResId)
    }
}
