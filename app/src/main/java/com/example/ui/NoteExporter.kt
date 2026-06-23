package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.core.content.FileProvider
import com.example.data.DecryptedNote
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoteExporter {

    private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val PRESENTABLE_DATE_FORMAT = SimpleDateFormat("EEEE, MMMM dd, yyyy - hh:mm a", Locale.getDefault())

    /**
     * Parse custom markdown-like note syntax into a SpannableStringBuilder
     * tailored specifically for rendering high-contrast text on a white PDF page.
     */
    fun parseToSpannable(note: DecryptedNote, density: Float): SpannableStringBuilder {
        val builder = SpannableStringBuilder()

        // 1. Title Header
        val titleStart = builder.length
        builder.append(note.title.ifBlank { "Untitled Secured Note" })
        val titleEnd = builder.length
        builder.setSpan(StyleSpan(Typeface.BOLD), titleStart, titleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(RelativeSizeSpan(1.6f), titleStart, titleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(0xFF0A5C36.toInt()), titleStart, titleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // Rich Deep Emerald
        builder.append("\n")

        // 2. Metadata subtitle
        val metaStart = builder.length
        val formattedDate = PRESENTABLE_DATE_FORMAT.format(Date(note.updatedAt))
        builder.append("Decrypted Archive Record • Created: $formattedDate\n")
        val metaEnd = builder.length
        builder.setSpan(StyleSpan(Typeface.ITALIC), metaStart, metaEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(RelativeSizeSpan(0.85f), metaStart, metaEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(0xFF666666.toInt()), metaStart, metaEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // Safe Charcoal Gray

        // Spacing before body
        builder.append("\n")

        // 3. Document body parsing line-by-line
        val lines = note.content.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            val isH1 = line.startsWith("# ")
            val isH2 = line.startsWith("## ")
            val isBullet = line.startsWith("• ") || line.startsWith("* ") || line.startsWith("- ")
            val isNumList = line.trim().isNotEmpty() && line.first().isDigit() && line.contains(". ")

            val cleanLine = when {
                isH1 -> line.substring(2)
                isH2 -> line.substring(3)
                isBullet -> "• " + line.substring(2)
                else -> line
            }

            val lineStart = builder.length
            
            // Parse inline tags in this line
            parseInlineToSpannable(builder, cleanLine)
            
            val lineEnd = builder.length

            // Apply block lines formatting
            when {
                isH1 -> {
                    builder.setSpan(StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(RelativeSizeSpan(1.3f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(0xFF0A5C36.toInt()), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                isH2 -> {
                    builder.setSpan(StyleSpan(Typeface.BOLD), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(RelativeSizeSpan(1.15f), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(0xFF1E704C.toInt()), lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                isBullet -> {
                    // Styled leading bullet
                    builder.setSpan(StyleSpan(Typeface.BOLD), lineStart, lineStart + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(0xFF0A5C36.toInt()), lineStart, lineStart + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                isNumList -> {
                    val dotIdx = cleanLine.indexOf(". ")
                    if (dotIdx != -1) {
                        builder.setSpan(StyleSpan(Typeface.BOLD), lineStart, lineStart + dotIdx + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        builder.setSpan(ForegroundColorSpan(0xFF0A5C36.toInt()), lineStart, lineStart + dotIdx + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }

            if (lineIdx < lines.size - 1) {
                builder.append("\n")
            }
        }

        return builder
    }

    private fun parseInlineToSpannable(builder: SpannableStringBuilder, line: String) {
        var i = 0
        val n = line.length
        val defaultTextColour = 0xFF222222.toInt() // high-contrast screen charcoal

        while (i < n) {
            // Bold Parser
            if (i < n - 1 && line[i] == '*' && line[i + 1] == '*') {
                val endBold = line.indexOf("**", i + 2)
                if (endBold != -1) {
                    val boldContent = line.substring(i + 2, endBold)
                    val start = builder.length
                    parseInlineToSpannable(builder, boldContent)
                    val end = builder.length
                    builder.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    i = endBold + 2
                    continue
                }
            }

            // Italic Parser
            if (line[i] == '*') {
                val endItalic = line.indexOf('*', i + 1)
                if (endItalic != -1) {
                    val italicContent = line.substring(i + 1, endItalic)
                    val start = builder.length
                    parseInlineToSpannable(builder, italicContent)
                    val end = builder.length
                    builder.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    i = endItalic + 1
                    continue
                }
            }

            // Underline Parser
            if (i < n - 2 && line.substring(i, minOf(i + 3, n)) == "<u>") {
                val endUnderline = line.indexOf("</u>", i + 3)
                if (endUnderline != -1) {
                    val content = line.substring(i + 3, endUnderline)
                    val start = builder.length
                    parseInlineToSpannable(builder, content)
                    val end = builder.length
                    builder.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    i = endUnderline + 4
                    continue
                }
            }

            // Colors Parser: <color=emerald>Text</color>
            if (i < n - 6 && line.substring(i, minOf(i + 7, n)) == "<color=") {
                val closedBracket = line.indexOf('>', i + 7)
                if (closedBracket != -1) {
                    val colorVal = line.substring(i + 7, closedBracket)
                    val endTag = line.indexOf("</color>", closedBracket + 1)
                    if (endTag != -1) {
                        val colorContent = line.substring(closedBracket + 1, endTag)
                        val start = builder.length
                        parseInlineToSpannable(builder, colorContent)
                        val end = builder.length

                        // Map tag color to printer-friendly high-contrast hex code
                        val printColor = when (colorVal.lowercase()) {
                            "emerald", "neon" -> 0xFF0A5C36.toInt()  // Rich deep dark green
                            "red" -> 0xFF990000.toInt()             // Rich dark crimson
                            "amber" -> 0xFF885500.toInt()           // Deep protective amber
                            "slate" -> 0xFF475569.toInt()           // Steel slate grey
                            "blue" -> 0xFF1D4ED8.toInt()            // Rich royal blue
                            "gold" -> 0xFFB45309.toInt()            // Golden-brown
                            "purple" -> 0xFF6B21A8.toInt()          // Dark Tyrian purple
                            else -> defaultTextColour
                        }

                        builder.setSpan(ForegroundColorSpan(printColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        i = endTag + 8
                        continue
                    }
                }
            }

            builder.append(line[i])
            i++
        }
    }

    /**
     * Export multiple selected notes into a single beautifully consolidated PDF document.
     */
    fun exportToPdf(context: Context, notes: List<DecryptedNote>): File? {
        if (notes.isEmpty()) return null

        val pdfDocument = PdfDocument()
        val density = context.resources.displayMetrics.density

        // Page definition: standard A4 is 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // Page Margins
        val marginLeft = 54f
        val marginRight = 54f
        val marginTop = 54f
        val marginBottom = 54f
        val printableWidth = pageWidth - (marginLeft + marginRight).toInt()

        // Text setup
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f * density
            color = 0xFF222222.toInt()
        }

        // Header and Footer paint
        val decorativePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f * density
            color = 0xFF888888.toInt()
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        var currentPageNumber = 1
        var currentNoteIndex = 1

        for (note in notes) {
            val spannable = parseToSpannable(note, density)

            val builder = StaticLayout.Builder.obtain(spannable, 0, spannable.length, textPaint, printableWidth)
                .setLineSpacing(0.0f, 1.25f)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            val staticLayout = builder.build()

            // Start a new page for each note
            var currentPage = pdfDocument.startPage(pageInfo)
            var canvas = currentPage.canvas
            var currentY = marginTop

            fun drawDecorativeHeader(c: Canvas, num: Int, title: String) {
                val headerText = "SAFE NOTES ENCRYPTED ARCHIVE • NOTE $currentNoteIndex OF ${notes.size}"
                c.drawText(headerText, marginLeft, 32f, decorativePaint)
                // Accent Emerald green decorative thin divider line
                val linePaint = Paint().apply {
                    color = 0xFF0A5C36.toInt()
                    strokeWidth = 1f
                }
                c.drawLine(marginLeft, 38f, pageWidth - marginRight, 38f, linePaint)
            }

            fun drawDecorativeFooter(c: Canvas, num: Int) {
                val footerText = "CONFIDENTIAL RECORD • PAGE $num"
                val textWidth = decorativePaint.measureText(footerText)
                c.drawText(footerText, (pageWidth - textWidth) / 2f, pageHeight - 32f, decorativePaint)
            }

            // Draw header on the first page of this note
            drawDecorativeHeader(canvas, currentPageNumber, note.title)

            for (line in 0 until staticLayout.lineCount) {
                val lineTop = staticLayout.getLineTop(line)
                val lineBottom = staticLayout.getLineBottom(line)
                val lineHeight = lineBottom - lineTop

                // Check page overrun
                if (currentY + lineHeight > pageHeight - marginBottom) {
                    // Complete previous page
                    drawDecorativeFooter(canvas, currentPageNumber)
                    pdfDocument.finishPage(currentPage)

                    // Provision next page
                    currentPageNumber++
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    currentY = marginTop

                    // Draw headers for this next page of same note
                    drawDecorativeHeader(canvas, currentPageNumber, note.title)
                }

                // Render specific line
                canvas.save()
                canvas.clipRect(marginLeft, currentY, marginLeft + printableWidth, currentY + lineHeight)
                canvas.translate(marginLeft, currentY - lineTop)
                staticLayout.draw(canvas)
                canvas.restore()

                currentY += lineHeight
            }

            // Close final page of this note
            drawDecorativeFooter(canvas, currentPageNumber)
            pdfDocument.finishPage(currentPage)
            
            currentNoteIndex++
            currentPageNumber++
        }

        return try {
            val timestamp = DATE_FORMAT.format(Date())
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val suffix = if (notes.size == 1) notes[0].title.replace("[^a-zA-Z0-9]".toRegex(), "_") else "MultiExport"
            val file = File(exportDir, "SafeNotes_${suffix}_$timestamp.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Export multiple selected notes into a single clear, readable Plain Text markdown file.
     */
    fun exportToTxt(context: Context, notes: List<DecryptedNote>): File? {
        if (notes.isEmpty()) return null

        val stringBuilder = StringBuilder()
        val separator = "\n\n${"=".repeat(60)}\n\n"

        notes.forEachIndexed { idx, note ->
            val formattedDate = PRESENTABLE_DATE_FORMAT.format(Date(note.updatedAt))
            stringBuilder.append("TITLE: ${note.title}\n")
            stringBuilder.append("MODIFIED: $formattedDate\n")
            stringBuilder.append("SECURITY CLASSIFICATION: ${if (note.isSensitive) "HIGHLY SENSITIVE" else "STANDARD"}\n")
            stringBuilder.append("-".repeat(40)).append("\n\n")
            stringBuilder.append(note.content)
            
            if (idx < notes.size - 1) {
                stringBuilder.append(separator)
            }
        }

        return try {
            val timestamp = DATE_FORMAT.format(Date())
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val suffix = if (notes.size == 1) notes[0].title.replace("[^a-zA-Z0-9]".toRegex(), "_") else "MultiExport"
            val file = File(exportDir, "SafeNotes_${suffix}_$timestamp.txt")
            val outputStream = FileOutputStream(file)
            outputStream.write(stringBuilder.toString().toByteArray())
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Pop standard system UI Share sheet to broadcast the exported file to user choice (Google Drive, Gmail, Files, WhatsApp, etc.)
     */
    fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Secure Notes Archive Export")
                putExtra(Intent.EXTRA_TEXT, "Exported personal records from Safe Notes encrypted database.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Save or Export Secure Archive using...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
