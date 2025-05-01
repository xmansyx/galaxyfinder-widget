package ris58h.galaxyfinder.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

/**
 * This activity is used as an entry point when the user wants to reconfigure 
 * an existing widget (through long press menu)
 */
class WidgetConfigureClickActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the widget ID from the intent
        val extras = intent.extras
        var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        
        if (extras != null) {
            widgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, 
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        
        // If we can't determine which widget needs to be configured, just exit
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(this, "Could not determine which widget to configure", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Launch the configuration activity for this widget
        val configIntent = Intent(this, ConfigurationActivity::class.java)
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        configIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(configIntent)
        
        // Close this bridging activity
        finish()
    }
} 
