package com.rahul.quickfiles

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.text.format.Formatter
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : Activity() {

    private data class Cat(val labelRes: Int, val docId: String, val emoji: String, val tint: String)

    private val cats = listOf(
        Cat(R.string.downloads, "primary:Download", "⬇️", "#2F6BFF"),
        Cat(R.string.images, "primary:Pictures", "🖼️", "#EC4899"),
        Cat(R.string.camera, "primary:DCIM", "📷", "#F59E0B"),
        Cat(R.string.videos, "primary:Movies", "🎬", "#8B5CF6"),
        Cat(R.string.music, "primary:Music", "🎵", "#EF4444"),
        Cat(R.string.documents, "primary:Documents", "📄", "#10B981")
    )

    private val MATCH = LinearLayout.LayoutParams.MATCH_PARENT
    private val WRAP = LinearLayout.LayoutParams.WRAP_CONTENT
    private var dark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Launched from a home-screen shortcut: open that folder directly.
        val doc = intent?.getStringExtra("doc")
        if (doc != null) {
            openFolder(doc)
            finish()
            return
        }
        buildUi()
    }

    // ---------- helpers ----------

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun c(hex: String) = Color.parseColor(hex)

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun ripple(fill: Int, radiusDp: Int, rippleColor: Int) = RippleDrawable(
        ColorStateList.valueOf(rippleColor),
        rounded(fill, radiusDp),
        rounded(Color.WHITE, radiusDp)
    )

    private fun tv(text: String, sp: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    @Suppress("DEPRECATION")
    private fun styleBars(bg: Int) {
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val ctrl = WindowCompat.getInsetsController(window, window.decorView)
        ctrl.isAppearanceLightStatusBars = !dark
        ctrl.isAppearanceLightNavigationBars = !dark
    }

    // ---------- UI ----------

    private fun buildUi() {
        dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        val bg = c(if (dark) "#0E1116" else "#F4F6FB")
        val surface = c(if (dark) "#1A1D24" else "#FFFFFF")
        val textPrimary = c(if (dark) "#F1F5F9" else "#0F172A")
        val textSecondary = c(if (dark) "#94A3B8" else "#64748B")
        val accent = c("#2F6BFF")

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(32))
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isVerticalScrollBarEnabled = false
            addView(content)
        }
        setContentView(scroll)
        styleBars(bg)

        ViewCompat.setOnApplyWindowInsetsListener(scroll) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Header
        content.addView(tv(getString(R.string.app_name), 30f, textPrimary, true))
        content.addView(
            tv(getString(R.string.tagline), 15f, textSecondary),
            LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(2) }
        )

        // Storage card
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes
        val free = stat.availableBytes
        val usedPct = if (total > 0) ((total - free) * 100 / total).toInt().coerceIn(0, 100) else 0

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(c("#2F6BFF"), c("#7C4DFF"))
            ).apply { cornerRadius = dp(26).toFloat() }
            setPadding(dp(22), dp(20), dp(22), dp(22))
            elevation = dp(4).toFloat()
        }
        card.addView(tv(getString(R.string.storage), 14f, Color.argb(215, 255, 255, 255), true))
        card.addView(tv("$usedPct%", 44f, Color.WHITE, true))

        val track = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 100f
            background = rounded(Color.argb(70, 255, 255, 255), 6)
        }
        track.addView(
            View(this).apply { background = rounded(Color.WHITE, 6) },
            LinearLayout.LayoutParams(0, MATCH, usedPct.toFloat())
        )
        card.addView(
            track,
            LinearLayout.LayoutParams(MATCH, dp(10)).apply {
                topMargin = dp(8)
                bottomMargin = dp(12)
            }
        )
        card.addView(
            tv(
                getString(
                    R.string.free_of,
                    Formatter.formatFileSize(this, free),
                    Formatter.formatFileSize(this, total)
                ),
                13f, Color.argb(225, 255, 255, 255)
            )
        )
        content.addView(card, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(20) })

        // Primary button
        val openBtn = TextView(this).apply {
            text = "📂   " + getString(R.string.open_fm)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = ripple(accent, 18, Color.argb(70, 255, 255, 255))
            elevation = dp(3).toFloat()
            isClickable = true
            setOnClickListener { openFileManager() }
        }
        content.addView(openBtn, LinearLayout.LayoutParams(MATCH, dp(60)).apply { topMargin = dp(18) })

        // Category tiles (2 per row)
        cats.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { cat ->
                row.addView(
                    tile(cat, surface, textPrimary),
                    LinearLayout.LayoutParams(0, WRAP, 1f).apply { setMargins(dp(6), dp(6), dp(6), dp(6)) }
                )
            }
            content.addView(row, LinearLayout.LayoutParams(MATCH, WRAP).apply {
                topMargin = dp(6)
                leftMargin = -dp(6)
                rightMargin = -dp(6)
            })
        }
    }

    private fun tile(cat: Cat, surface: Int, textColor: Int): LinearLayout {
        val tint = c(cat.tint)
        val badgeBg = ColorUtils.setAlphaComponent(tint, if (dark) 60 else 38)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = ripple(surface, 22, ColorUtils.setAlphaComponent(tint, 50))
            elevation = dp(1).toFloat()
            isClickable = true
            setOnClickListener { openFolder(cat.docId) }
        }
        val badge = TextView(this).apply {
            text = cat.emoji
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            gravity = Gravity.CENTER
            background = rounded(badgeBg, 16)
        }
        col.addView(badge, LinearLayout.LayoutParams(dp(48), dp(48)))
        col.addView(
            tv(getString(cat.labelRes), 15f, textColor, true),
            LinearLayout.LayoutParams(WRAP, WRAP).apply { topMargin = dp(12) }
        )
        return col
    }

    // ---------- actions ----------

    private fun openFileManager() {
        val candidates = listOf(
            Intent(Intent.ACTION_VIEW).setClassName(
                "com.android.documentsui", "com.android.documentsui.files.FilesActivity"
            ),
            Intent(Intent.ACTION_VIEW).setClassName(
                "com.google.android.documentsui", "com.android.documentsui.files.FilesActivity"
            ),
            Intent("android.provider.action.BROWSE").setData(
                DocumentsContract.buildRootUri("com.android.externalstorage.documents", "primary")
            ),
            Intent("android.intent.action.VIEW_DOWNLOADS")
        )
        for (i in candidates) {
            try {
                startActivity(i)
                return
            } catch (e: Exception) {
                // try next
            }
        }
        Toast.makeText(this, R.string.not_found, Toast.LENGTH_LONG).show()
    }

    private fun openFolder(docId: String) {
        try {
            val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", docId)
            startActivity(
                Intent(Intent.ACTION_VIEW).setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
            )
        } catch (e: Exception) {
            openFileManager()
        }
    }
}
