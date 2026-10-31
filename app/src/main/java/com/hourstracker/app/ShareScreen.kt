package com.hourstracker.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun ShareScreen(vm: TrackerViewModel) {
    val s = LocalStrings.current
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val settings = vm.settings
    val weekStart = vm.weekStart
    val week = buildWeek(weekStart, vm.entries, settings)
    val text = buildReportText(weekStart, week, settings, s)
    val baseName = Exporter.baseName(weekStart)

    fun makePdf() = Exporter.pdf(weekStart, week, settings, s)
    fun makeCsv() = Exporter.csv(weekStart, week, settings, s)
    fun makeXlsx() = Exporter.xlsx(weekStart, week, settings, s)

    // "Save" lets the person pick where the file goes (Downloads, Drive...).
    val savePdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Exporter.MIME_PDF)) { uri ->
        if (uri != null) writeToUri(context, uri, s) { makePdf() }
    }
    val saveCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Exporter.MIME_CSV)) { uri ->
        if (uri != null) writeToUri(context, uri, s) { makeCsv() }
    }
    val saveXlsx = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Exporter.MIME_XLSX)) { uri ->
        if (uri != null) writeToUri(context, uri, s) { makeXlsx() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = s.shareIntro,
            color = scheme.onSurfaceVariant,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        GroupCard {
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, s.appTitle)
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(send, s.chooserTitle))
                },
                modifier = Modifier.weight(1f)
            ) { Text(s.shareText) }

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(s.appTitle, text))
                    Toast.makeText(context, s.copied, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) { Text(s.copy) }
        }

        SectionHeader(text = s.exportTitle)
        GroupCard {
            ExportRow(
                label = s.filePdf,
                onShare = { shareFile(context, baseName + ".pdf", Exporter.MIME_PDF, s) { makePdf() } },
                onSave = { savePdf.launch(baseName + ".pdf") }
            )
            RowDivider()
            ExportRow(
                label = s.fileCsv,
                onShare = { shareFile(context, baseName + ".csv", Exporter.MIME_CSV, s) { makeCsv() } },
                onSave = { saveCsv.launch(baseName + ".csv") }
            )
            RowDivider()
            ExportRow(
                label = s.fileExcel,
                onShare = { shareFile(context, baseName + ".xlsx", Exporter.MIME_XLSX, s) { makeXlsx() } },
                onSave = { saveXlsx.launch(baseName + ".xlsx") }
            )
        }
    }
}

@Composable
private fun ExportRow(label: String, onShare: () -> Unit, onSave: () -> Unit) {
    val s = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            modifier = Modifier.weight(1f)
        )
        Pill(text = s.fileShare, onClick = onShare)
        Pill(text = s.fileSave, onClick = onSave)
    }
}

/** Writes the file into the cache and hands it to the Android share sheet. */
private fun shareFile(context: Context, fileName: String, mime: String, s: Strings, bytes: () -> ByteArray) {
    try {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(bytes())
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, s.chooserTitle))
    } catch (e: Exception) {
        Toast.makeText(context, s.exportFailed, Toast.LENGTH_LONG).show()
    }
}

/** Writes the file to the place the person chose in the system file picker. */
private fun writeToUri(context: Context, uri: Uri, s: Strings, bytes: () -> ByteArray) {
    try {
        val stream = context.contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("No output stream")
        stream.use { it.write(bytes()) }
        Toast.makeText(context, s.saved, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, s.exportFailed, Toast.LENGTH_LONG).show()
    }
}
