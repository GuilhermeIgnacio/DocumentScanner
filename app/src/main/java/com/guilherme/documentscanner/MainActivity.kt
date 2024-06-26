package com.guilherme.documentscanner

import android.content.ContentValues
import android.net.Uri
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
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.guilherme.documentscanner.di.initKoin
import com.guilherme.documentscanner.presentation.MainViewModel
import com.guilherme.documentscanner.presentation.MainViewModelEvents
import com.guilherme.documentscanner.ui.theme.AppTheme
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

                                    val inputStream = this@MainActivity.contentResolver.openInputStream(pdfUri)
                                    val pdfBytes = inputStream?.readBytes()

                                    val contentValues = ContentValues().apply {
                                        put(MediaStore.MediaColumns.DISPLAY_NAME, "scanned_document_${System.currentTimeMillis()}.pdf")
                                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                                    }

                                    val resolver = this@MainActivity.contentResolver
                                    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

                                    uri?.let {
                                        resolver.openOutputStream(it)?.use { outputStream ->
                                            outputStream.write(pdfBytes)
                                            outputStream.close()
                                            Toast.makeText(this@MainActivity, "PDF salvo em: $uri", Toast.LENGTH_LONG).show()
                                        } ?: run {
                                            Toast.makeText(this@MainActivity, "Erro ao salvar PDF: OutputStream nulo", Toast.LENGTH_LONG).show()
                                        }
                                    } ?: run {
                                        Toast.makeText(this@MainActivity, "Erro ao salvar PDF: URI nulo", Toast.LENGTH_LONG).show()
                                    }

                                }



                            }

                        }
                    }
                )

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {

                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(state.data) {

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable { /*Todo: Open Detail Sheet*/ },
                                    shadowElevation = 16.dp,
                                    shape = RoundedCornerShape(10)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AsyncImage(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(10)),
                                            model = it.uri[0].toUri(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                        )
                                    }
                                }


                            }
                        }

                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(80.dp),
                            onClick = {
                                scanner.getStartScanIntent(this@MainActivity)
                                    .addOnSuccessListener { intentSender ->
                                        scannerLauncher.launch(
                                            IntentSenderRequest.Builder(
                                                intentSender
                                            ).build()
                                        )
                                    }
                                    .addOnFailureListener {
                                        println(it)
                                    }
                            },
                            shape = CircleShape,
                            contentColor = Color.White
                        ) {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(id = R.drawable.outline_scan_24),
                                contentDescription = "Scan Icon"
                            )
                        }
                    }
                }
            }

        }
    }
}