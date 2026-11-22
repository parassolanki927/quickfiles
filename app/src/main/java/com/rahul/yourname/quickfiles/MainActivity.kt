package com.rahul.quickfiles

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.text.format.Formatter
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : Activity() {

    private val folders = listOf(
        R.string.downloads to "primary:Download",
        R.string.images to "primary:Pictures",
        R.string.camera to "primary:DCIM",
        R.string.videos to "primary:Movies",
        R.string.music to "primary:Music",
        R.string.documents to "primary:Documents"
    )

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

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun roundBg(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun label(text: String, sp: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun styledButton(text: String, bg: Int, fg: Int, onClick: () -> Unit) =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            setTextColor(fg)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            background = roundBg(bg, 14)
            stateListAnimator = null
            setOnClickListener { onClick() }
        }

    private fun buildUi() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
            addView(content)
        }
        setContentView(scroll)
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        content.addView(label(getString(R.string.app_name), 28f, Color.parseColor("#0F172A"), true))
        content.addView(label(getString(R.string.tagline), 15f, Color.parseColor("#64748B")))

        // Storage card
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes
        val free = stat.availableBytes
        val usedPct = if (total > 0) ((total - free) * 100 / total).toInt() else 0

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundBg(Color.parseColor("#F1F5F9"), 16)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        card.addView(label(getString(R.string.storage), 16f, Color.parseColor("#0F172A"), true))
        card.addView(ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = usedPct
        }, LinearLayout.LayoutParams(-1, dp(24)))
        card.addView(
            label(
                getString(
                    R.string.free_of,
                    Formatter.formatFileSize(this, free),
                    Formatter.formatFileSize(this, total)
                ), 14f, Color.parseColor("#475569")
            )
        )
        content.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(20) })

        // Main button
        content.addView(
            styledButton(getString(R.string.open_fm), Color.parseColor("#1E88E5"), Color.WHITE) { openFileManager() },
            LinearLayout.LayoutParams(-1, dp(60)).apply { topMargin = dp(20) }
        )

        // Folder grid (2 per row)
        folders.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { (res, id) ->
                row.addView(
                    styledButton(getString(res), Color.parseColor("#E3F2FD"), Color.parseColor("#0D47A1")) { openFolder(id) },
                    LinearLayout.LayoutParams(0, dp(56), 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
                )
            }
            content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        }
    }

    private fun openFileManager() {
        val candidates = listOf(
            Intent(Intent.ACTION_VIEW).setClassName("com.android.documentsui", "com.android.documentsui.files.FilesActivity"),
            Intent(Intent.ACTION_VIEW).setClassName("com.google.android.documentsui", "com.android.documentsui.files.FilesActivity"),
            Intent("android.provider.action.BROWSE").setData(
                DocumentsContract.buildRootUri("com.android.externalstorage.documents", "primary")
            ),
            Intent("android.intent.action.VIEW_DOWNLOADS")
        )
        for (i in candidates) {
            try {
                startActivity(i)
                return
            } catch (_: Exception) {
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
        } catch (_: Exception) {
            openFileManager()
        }
    }
}
