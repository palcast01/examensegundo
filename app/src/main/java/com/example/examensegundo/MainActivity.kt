package com.example.examensegundo



import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    var token by remember { mutableStateOf("") }
    var userToken by remember { mutableStateOf("") }
    var showTokenField by remember { mutableStateOf(false) }
    var tokenTimer by remember { mutableStateOf(15) }
    var tokenExpired by remember { mutableStateOf(false) }

    val isCurpValid = curp.length == 18 && Regex("^[A-Z0-9]{18}$").matches(curp)
    val isTokenValid = token == userToken && !tokenExpired
    val isFormValid = nombre.isNotBlank() && isCurpValid && capturedBitmap != null && isTokenValid

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            capturedBitmap = it
            val file = saveBitmapToFile(context, it)
            imageUri = Uri.fromFile(file)
            // Generar token al tomar la foto
            token = (100..999).random().toString()
            tokenTimer = 15
            tokenExpired = false
            userToken = ""
            showTokenField = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (!hasLocationPermission) {
            Toast.makeText(context, "Debes activar la ubicación", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    LaunchedEffect(Unit) {
        val camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val location = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasCameraPermission = camera
        hasLocationPermission = location

        if (!camera || !location) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    LaunchedEffect(token) {
        if (showTokenField) {
            while (tokenTimer > 0) {
                delay(1000)
                tokenTimer--
            }
            tokenExpired = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = nombre,
            onValueChange = { nombre = it },
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth().padding(8.dp).height(56.dp), contentAlignment = Alignment.CenterStart) {
                    if (nombre.isEmpty()) Text("Nombre completo")
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = curp,
            onValueChange = { curp = it.uppercase() }, // CURP en mayúsculas
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth().padding(8.dp).height(56.dp), contentAlignment = Alignment.CenterStart) {
                    if (curp.isEmpty()) Text("CURP")
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Foto tomada",
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (hasCameraPermission && hasLocationPermission) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(context, "Faltan permisos", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Tomar Foto")
        }

        if (showTokenField) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Token: $token (Caduca en $tokenTimer segundos)")
            BasicTextField(
                value = userToken,
                onValueChange = { userToken = it },
                decorationBox = { innerTextField ->
                    Box(Modifier.fillMaxWidth().padding(8.dp).height(56.dp), contentAlignment = Alignment.CenterStart) {
                        if (userToken.isEmpty()) Text("Ingresa token")
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    saveBitmapToGallery(context, capturedBitmap!!)
                    Toast.makeText(context, "Nombre: $nombre\nCURP: $curp\nFoto guardada", Toast.LENGTH_LONG).show()

                    // Limpiar campos
                    nombre = ""
                    curp = ""
                    capturedBitmap = null
                    imageUri = null
                    token = ""
                    userToken = ""
                    showTokenField = false
                    tokenTimer = 15
                    tokenExpired = false

                } else {
                    Toast.makeText(context, "Información incorrecta o incompleta", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isFormValid
        ) {
            Text("Aceptar")
        }
    }
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.externalCacheDir, "JPEG_${timeStamp}.jpg")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
    }
    return file
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/ExamenFusionado")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val contentResolver = context.contentResolver
    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        contentResolver.openOutputStream(it)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(it, contentValues, null, null)
        }
    }
}
