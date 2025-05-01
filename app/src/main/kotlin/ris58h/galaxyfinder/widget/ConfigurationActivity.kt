package ris58h.galaxyfinder.widget

import android.app.Activity
import android.app.UiModeManager
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import yuku.ambilwarna.AmbilWarnaDialog

class ConfigurationActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    // UI elements
    private lateinit var bgColorGroup: RadioGroup
    private lateinit var textColorGroup: RadioGroup
    private lateinit var bgCustomOptions: LinearLayout
    private lateinit var textCustomOptions: LinearLayout
    private lateinit var bgTransparencySlider: SeekBar
    private lateinit var widgetPreview: TextView
    private lateinit var transparencyPercentage: TextView
    private lateinit var bgColorPreview: View
    private lateinit var textColorPreview: View
    private lateinit var previewContainer: FrameLayout
    
    // Color values
    private var customBgColor = 0
    private var customTextColor = 0
    private var bgTransparency = 128 // Default half transparent (0-255)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set status bar icon color based on theme
        updateStatusBarIconColor()
        
        // Add entry animation for API 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set up slide-up animation for the entire activity
            window.enterTransition = android.transition.Explode().apply {
                duration = 350
                interpolator = AccelerateDecelerateInterpolator()
            }
            
            window.exitTransition = android.transition.Explode().apply {
                duration = 250
                interpolator = AccelerateDecelerateInterpolator()
            }
        }
        
        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)
        
        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        
        // If we're being launched from Samsung's reconfiguration and don't have a widget ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID && 
            intent.action == "com.sec.android.widgetapp.APPWIDGET_RECONFIGURE") {
            // Try to get the widget ID directly from the component name
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val componentName = ComponentName(this, FinderWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            // Just use the first widget ID if any exist
            if (appWidgetIds.isNotEmpty()) {
                appWidgetId = appWidgetIds[0]
            }
        }
        
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(this, "Error: No widget ID found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Set the content view
        setContentView(R.layout.configuration_activity)
        
        // Get UI references
        bgColorGroup = findViewById(R.id.background_color_group)
        textColorGroup = findViewById(R.id.text_color_group)
        bgCustomOptions = findViewById(R.id.bg_color_custom_options)
        textCustomOptions = findViewById(R.id.text_color_custom_options)
        bgTransparencySlider = findViewById(R.id.bg_transparency_slider)
        transparencyPercentage = findViewById(R.id.transparency_percentage)
        previewContainer = findViewById(R.id.preview_container)
        widgetPreview = findViewById<View>(R.id.widget_preview_layout).findViewById(R.id.widget_preview)
        bgColorPreview = findViewById(R.id.bg_color_preview)
        textColorPreview = findViewById(R.id.text_color_preview)
        
        // Set the current wallpaper as the preview background
        setWallpaperBackground()
        
        // Load current color preferences
        val prefs = WidgetPreferences(this)
        val currentBackgroundColor = prefs.getBackgroundColor(appWidgetId)
        val currentTextColor = prefs.getTextColor(appWidgetId)
        
        // Load custom color values
        customBgColor = prefs.getCustomBackgroundColor(appWidgetId)
        customTextColor = prefs.getCustomTextColor(appWidgetId)
        bgTransparency = prefs.getBackgroundTransparency(appWidgetId)
        
        // Set initial values for the UI
        bgTransparencySlider.progress = bgTransparency
        updateTransparencyLabel(bgTransparency)
        
        // Set color previews
        bgColorPreview.setBackgroundColor(customBgColor)
        textColorPreview.setBackgroundColor(customTextColor)
        
        // Set up background color selection
        when (currentBackgroundColor) {
            WidgetPreferences.BG_COLOR_DARK -> bgColorGroup.check(R.id.bg_color_dark)
            WidgetPreferences.BG_COLOR_LIGHT -> bgColorGroup.check(R.id.bg_color_light)
            WidgetPreferences.BG_COLOR_CUSTOM -> {
                bgColorGroup.check(R.id.bg_color_custom)
                bgCustomOptions.visibility = View.VISIBLE
            }
            else -> bgColorGroup.check(R.id.bg_color_transparent) // Default
        }
        
        // Set up text color selection
        when (currentTextColor) {
            WidgetPreferences.TEXT_COLOR_DARK -> textColorGroup.check(R.id.text_color_dark)
            WidgetPreferences.TEXT_COLOR_CUSTOM -> {
                textColorGroup.check(R.id.text_color_custom)
                textCustomOptions.visibility = View.VISIBLE
            }
            else -> textColorGroup.check(R.id.text_color_light) // Default
        }
        
        // Setup RadioGroup listeners for showing/hiding custom options
        bgColorGroup.setOnCheckedChangeListener { _, checkedId ->
            bgCustomOptions.visibility = if (checkedId == R.id.bg_color_custom) View.VISIBLE else View.GONE
            updatePreview()
        }
        
        textColorGroup.setOnCheckedChangeListener { _, checkedId ->
            textCustomOptions.visibility = if (checkedId == R.id.text_color_custom) View.VISIBLE else View.GONE
            updatePreview()
        }
        
        // Setup color picker buttons
        val bgColorPickerButton = findViewById<Button>(R.id.bg_color_picker_button)
        bgColorPickerButton.setOnClickListener {
            openColorPicker(customBgColor) { color ->
                customBgColor = color
                bgColorPreview.setBackgroundColor(color)
                updatePreview()
            }
        }
        
        val textColorPickerButton = findViewById<Button>(R.id.text_color_picker_button)
        textColorPickerButton.setOnClickListener {
            openColorPicker(customTextColor) { color ->
                customTextColor = color
                textColorPreview.setBackgroundColor(color)
                updatePreview()
            }
        }
        
        // Setup transparency slider
        bgTransparencySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                bgTransparency = progress
                updateTransparencyLabel(progress)
                updatePreview()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        
        // Add reset button click listener
        val resetButton = findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            // Reset to defaults
            bgColorGroup.check(R.id.bg_color_transparent)
            textColorGroup.check(R.id.text_color_light)
            bgCustomOptions.visibility = View.GONE
            textCustomOptions.visibility = View.GONE
            customBgColor = WidgetPreferences.DEFAULT_CUSTOM_BG_COLOR
            customTextColor = WidgetPreferences.DEFAULT_CUSTOM_TEXT_COLOR
            bgTransparency = WidgetPreferences.DEFAULT_TRANSPARENCY
            bgTransparencySlider.progress = bgTransparency
            updatePreview()
        }
        
        // Add save button click listener
        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            // Save the selected preferences
            val selectedBgColorId = bgColorGroup.checkedRadioButtonId
            val selectedTextColorId = textColorGroup.checkedRadioButtonId
            
            val bgColor = when (selectedBgColorId) {
                R.id.bg_color_dark -> WidgetPreferences.BG_COLOR_DARK
                R.id.bg_color_light -> WidgetPreferences.BG_COLOR_LIGHT
                R.id.bg_color_custom -> WidgetPreferences.BG_COLOR_CUSTOM
                else -> WidgetPreferences.BG_COLOR_TRANSPARENT
            }
            
            val textColor = when (selectedTextColorId) {
                R.id.text_color_dark -> WidgetPreferences.TEXT_COLOR_DARK
                R.id.text_color_custom -> WidgetPreferences.TEXT_COLOR_CUSTOM
                else -> WidgetPreferences.TEXT_COLOR_LIGHT
            }
            
            // Save all preferences
            prefs.saveBackgroundColor(appWidgetId, bgColor)
            prefs.saveTextColor(appWidgetId, textColor)
            prefs.saveCustomBackgroundColor(appWidgetId, customBgColor)
            prefs.saveCustomTextColor(appWidgetId, customTextColor)
            prefs.saveBackgroundTransparency(appWidgetId, bgTransparency)
            
            // Update the widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            updateAppWidget(this, appWidgetManager, appWidgetId)
            
            // Create the return intent and set the result
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
        
        // Initialize preview
        updatePreview()
        
        // Animate cards with a staggered effect
        animateCards()
    }
    
    private fun openColorPicker(initialColor: Int, onColorSelected: (Int) -> Unit) {
        val colorPicker = AmbilWarnaDialog(
            this,
            initialColor,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {
                    // Do nothing
                }
                
                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    onColorSelected(color)
                }
            }
        )
        colorPicker.show()
    }
    
    private fun updateTransparencyLabel(progress: Int) {
        val percentage = (progress * 100 / 255)
        transparencyPercentage.text = "$percentage%"
    }
    
    private fun updatePreview() {
        // Get the selected color options
        val selectedBgColorId = bgColorGroup.checkedRadioButtonId
        val selectedTextColorId = textColorGroup.checkedRadioButtonId
        
        // Apply background with transparency for all background types
        when (selectedBgColorId) {
            R.id.bg_color_dark -> {
                // Create a custom drawable for dark with transparency
                val darkColor = getColorCompat(this, R.color.widget_background_dark_color)
                val customBg = CustomBackgroundHelper.createCustomBackground(this, darkColor, bgTransparency)
                widgetPreview.background = customBg
            }
            R.id.bg_color_light -> {
                // Create a custom drawable for light with transparency
                val lightColor = getColorCompat(this, R.color.widget_background_light_color)
                val customBg = CustomBackgroundHelper.createCustomBackground(this, lightColor, bgTransparency)
                widgetPreview.background = customBg
            }
            R.id.bg_color_custom -> {
                // Create and apply a custom drawable with transparency
                val customBg = CustomBackgroundHelper.createCustomBackground(this, customBgColor, bgTransparency)
                widgetPreview.background = customBg
            }
            else -> {
                // For transparent, just apply transparency to a black/transparent background
                val customBg = CustomBackgroundHelper.createCustomBackground(this, Color.TRANSPARENT, bgTransparency)
                widgetPreview.background = customBg
            }
        }
        
        // Apply text color
        when (selectedTextColorId) {
            R.id.text_color_dark -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val textColor = resources.getColor(R.color.widget_text_dark, theme)
                    widgetPreview.setTextColor(textColor)
                    widgetPreview.setHintTextColor(textColor)
                } else {
                    @Suppress("DEPRECATION")
                    val textColor = resources.getColor(R.color.widget_text_dark)
                    widgetPreview.setTextColor(textColor)
                    widgetPreview.setHintTextColor(textColor)
                }
            }
            R.id.text_color_custom -> {
                widgetPreview.setTextColor(customTextColor)
                widgetPreview.setHintTextColor(customTextColor)
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val textColor = resources.getColor(R.color.widget_text_light, theme)
                    widgetPreview.setTextColor(textColor)
                    widgetPreview.setHintTextColor(textColor)
                } else {
                    @Suppress("DEPRECATION")
                    val textColor = resources.getColor(R.color.widget_text_light)
                    widgetPreview.setTextColor(textColor)
                    widgetPreview.setHintTextColor(textColor)
                }
            }
        }
    }
    
    private fun animateCards() {
        // Find all card views in the layout
        val cards = arrayOf(
            findViewById<View>(R.id.preview_card),
            findViewById<View>(R.id.background_card),
            findViewById<View>(R.id.text_color_card)
        )
        
        // Set initial state - off screen
        cards.forEach { 
            it.translationY = 100f
            it.alpha = 0f
        }
        
        // Animate each card with a delay
        cards.forEachIndexed { index, card ->
            card.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay((index * 100).toLong())
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }
    
    /**
     * Sets the current wallpaper as the background for the preview container
     */
    private fun setWallpaperBackground() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(this)
            val wallpaperDrawable = wallpaperManager.drawable
            
            // Apply the wallpaper to the preview container
            if (wallpaperDrawable != null) {
                previewContainer.background = wallpaperDrawable
            }
        } catch (e: Exception) {
            // Fallback to a dark background if we can't get the wallpaper
            previewContainer.setBackgroundColor(Color.argb(50, 0, 0, 0))
        }
    }
    
    // Override finish to add custom exit animation
    override fun finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Animate the root layout before finishing
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            
            // Create a slide-down transition
            val slideDown = Slide(Gravity.BOTTOM).apply {
                duration = 250
                addTarget(rootView.getChildAt(0))
                interpolator = AccelerateDecelerateInterpolator()
            }
            
            // Set a listener to call super.finish() after the animation
            slideDown.addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {
                    // Call the actual finish after animation completes
                    super@ConfigurationActivity.finish()
                }
                override fun onTransitionCancel(transition: Transition) {
                    super@ConfigurationActivity.finish()
                }
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionResume(transition: Transition) {}
            })
            
            // Run the transition
            TransitionManager.beginDelayedTransition(rootView, slideDown)
            
            // Hide the content to trigger the animation
            rootView.getChildAt(0).visibility = View.GONE
        } else {
            // For older devices, just finish normally
            super.finish()
            // Apply a basic slide animation using overridePendingTransition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
    
    /**
     * Updates the status bar icon color based on the current theme
     */
    private fun updateStatusBarIconColor() {
        try {
            // Check if we're in dark mode
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == 
                              Configuration.UI_MODE_NIGHT_YES

            if (Build.VERSION.SDK_INT >= 30) { // Android 11 (R)
                // Safely use reflection to check for WindowInsetsController
                try {
                    val insetsControllerClass = Class.forName("android.view.WindowInsetsController")
                    val appearanceLightStatusBars = insetsControllerClass
                        .getField("APPEARANCE_LIGHT_STATUS_BARS")
                        .getInt(null)

                    window.decorView.javaClass
                        .getMethod("getWindowInsetsController")
                        .invoke(window.decorView)?.let { controller ->
                            val setAppearanceMethod = controller.javaClass
                                .getMethod("setSystemBarsAppearance", Int::class.java, Int::class.java)
                            
                            if (!isNightMode) {
                                // Light mode - use dark status bar icons
                                setAppearanceMethod.invoke(controller, appearanceLightStatusBars, appearanceLightStatusBars)
                            } else {
                                // Dark mode - use light status bar icons
                                setAppearanceMethod.invoke(controller, 0, appearanceLightStatusBars)
                            }
                        }
                } catch (e: Exception) {
                    // Fall back to the older method if reflection fails
                    useOlderStatusBarMethod(isNightMode)
                }
            } else {
                // Use older method for Android 10 and below
                useOlderStatusBarMethod(isNightMode)
            }
        } catch (e: Exception) {
            // Fail silently - status bar color isn't critical
        }
    }
    
    /**
     * Use the older method for setting status bar icons on Android 6.0-10
     */
    private fun useOlderStatusBarMethod(isNightMode: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val decorView = window.decorView
                var flags = decorView.systemUiVisibility
                
                // Update the status bar icons color
                flags = if (!isNightMode) {
                    // Light theme needs dark icons
                    flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    // Dark theme needs light icons
                    flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                
                decorView.systemUiVisibility = flags
            } catch (e: Exception) {
                // Ignore - status bar appearance isn't critical
            }
        }
    }
    
    /**
     * Handle configuration changes such as theme changes
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Update status bar for theme changes
        updateStatusBarIconColor()
        
        // Recreate the activity to apply the new theme
        recreate()
    }
} 
