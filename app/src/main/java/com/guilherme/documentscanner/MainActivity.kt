package com.guilherme.documentscanner

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.guilherme.documentscanner.di.initKoin
import com.guilherme.documentscanner.presentation.MainViewModel
import com.guilherme.documentscanner.presentation.MainViewModelEvents
import com.guilherme.documentscanner.presentation.components.AppScreen
import com.guilherme.documentscanner.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
        val scanner = GmsDocumentScanning.getClient(options)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(), Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(), Color.Transparent.toArgb()
            )
        )

        initKoin()

        setContent {

            val mainViewModel: MainViewModel = koinViewModel()
            val state by mainViewModel.state.collectAsStateWithLifecycle()
            val onEvent = mainViewModel::onEvent

            AppTheme {

                val scannerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = {
                        if (it.resultCode == RESULT_OK) {
                            val result =
                                GmsDocumentScanningResult.fromActivityResultIntent(it.data)

                            onEvent(MainViewModelEvents.OnDocumentAdded(result?.pages?.map { data -> data.imageUri }))

                            if (result != null) {
                                result.getPdf()?.let { pdf ->
                                    val pdfUri = pdf.getUri()

                                    val inputStream =
                                        this@MainActivity.contentResolver.openInputStream(pdfUri)
                                    val pdfBytes = inputStream?.readBytes()

                                    val contentValues = ContentValues().apply {
                                        put(
                                            MediaStore.MediaColumns.DISPLAY_NAME,
                                            "scanned_document_${System.currentTimeMillis()}.pdf"
                                        )
                                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                                        put(
                                            MediaStore.MediaColumns.RELATIVE_PATH,
                                            Environment.DIRECTORY_DOCUMENTS
                                        )
                                    }

                                    val resolver = this@MainActivity.contentResolver
                                    val uri = resolver.insert(
                                        MediaStore.Files.getContentUri("external"),
                                        contentValues
                                    )

                                    uri?.let {
                                        resolver.openOutputStream(it)?.use { outputStream ->
                                            outputStream.write(pdfBytes)
                                            outputStream.close()
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(R.string.pdf_stored_at, uri),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } ?: run {
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(R.string.error_storing_pdf),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } ?: run {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.error_storing_pdf),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                }


                            }

                        }
                    }
                )

                AppScreen(
                    state = state,
                    onEvent = onEvent,
                    scanner = scanner,
                    scannerLauncher = scannerLauncher,
                    context = this@MainActivity
                )

            }

        }
    }

}