package ris58h.galaxyfinder.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

/**
 * Helper class for creating custom background drawables with transparency
 */
object CustomBackgroundHelper {
    
    /**
     * Creates a drawable with custom color and transparency
     * 
     * @param context The context
     * @param baseColor The base color (without alpha)
     * @param transparency The transparency value (0-255, where 0 is fully transparent and 255 is opaque)
     * @return A GradientDrawable with specified color and transparency
     */
    fun createCustomBackground(context: Context, baseColor: Int, transparency: Int): GradientDrawable {
        // Extract RGB components from base color
        val red = Color.red(baseColor)
        val green = Color.green(baseColor)
        val blue = Color.blue(baseColor)
        
        // Create a new color with the specified transparency
        val colorWithAlpha = Color.argb(transparency, red, green, blue)
        
        // Get the corner radius from resources
        val cornerRadius = context.resources.getDimension(R.dimen.widget_background_radius)
        
        // Create and return the drawable
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(colorWithAlpha)
            setCornerRadius(cornerRadius)
        }
    }
} 
