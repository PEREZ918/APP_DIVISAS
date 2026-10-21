package com.example.divisas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.Locale

// ---------- Red ----------
data class RatesResponse(val rates: Map<String, Double>)

interface FxApi {
    @GET("v6/latest/{base}")
    suspend fun latest(@Path("base") base: String): RatesResponse
}

private val api: FxApi = Retrofit.Builder()
    .baseUrl("https://open.er-api.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build().create(FxApi::class.java)


val CURRENCIES = listOf("USD", "EUR", "COP", "MXN", "GBP", "JPY", "BRL", "ARS", "CAD", "CHF")

class FxViewModel : ViewModel() {
    var amount by mutableStateOf("100")
    var from by mutableStateOf("USD")
    var to by mutableStateOf("COP")
    var rates by mutableStateOf<Map<String, Double>>(emptyMap())
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    val rate: Double get() = rates[to] ?: 0.0
    val result: Double get() = (amount.toDoubleOrNull() ?: 0.0) * rate

    init { load() }

    fun load() = viewModelScope.launch {
        loading = true
        try { rates = api.latest(from).rates; error = null }
        catch (e: Exception) { error = "Sin conexión. Intenta de nuevo." }
        loading = false
    }

    fun updateFrom(c: String) { from = c; load() }
    fun swap() { val t = from; from = to; to = t; load() }
}

// ---------- Activity ----------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF0B6E4F))) { FxScreen() } }
    }
}

private val Green = Color(0xFF0B6E4F)
private val GreenDark = Color(0xFF083D2E)

@Composable
fun FxScreen(vm: FxViewModel = viewModel()) {
    val ctx = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }

    // Permiso 1: CÁMARA
    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) showCamera = true
    }
    // Permiso 2: MICRÓFONO
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) { listening = true; listen(ctx, { vm.amount = it }) { listening = false } }
    }

    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(GreenDark, Green, Color(0xFFF4F7F5)), endY = 1400f))
            .statusBarsPadding().padding(20.dp)
    ) {
        Text("Divisas", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Convierte monedas en tiempo real", color = Color.White.copy(.8f))
        Spacer(Modifier.height(20.dp))

        // Tarjeta conversor
        Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = vm.amount, onValueChange = { vm.amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Monto") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    CurrencyPicker(vm.from) { vm.updateFrom(it) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { vm.swap() }) {
                        Icon(Icons.Default.SwapVert, "Intercambiar", tint = Green)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (vm.loading) "…" else String.format(Locale.US, "%,.2f", vm.result),
                        fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
                    )
                    CurrencyPicker(vm.to) { vm.to = it }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    vm.error ?: "1 ${vm.from} = ${String.format(Locale.US, "%,.4f", vm.rate)} ${vm.to}",
                    color = if (vm.error != null) Color.Red else Color.Gray, fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botones de hardware
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (granted(ctx, Manifest.permission.CAMERA)) showCamera = true
                    else camLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Cámara") }

            Button(
                onClick = {
                    if (granted(ctx, Manifest.permission.RECORD_AUDIO)) {
                        listening = true; listen(ctx, { vm.amount = it }) { listening = false }
                    } else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Icon(Icons.Default.Mic, null); Spacer(Modifier.width(8.dp))
                Text(if (listening) "Escuchando…" else "Dictar")
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Tasas rápidas", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        listOf("EUR", "COP", "MXN", "GBP").filter { it != vm.from }.forEach { c ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp))
                    .background(Color.White).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${vm.from} → $c", fontWeight = FontWeight.Medium)
                Text(vm.rates[c]?.let { String.format(Locale.US, "%,.4f", it) } ?: "—")
            }
        }
    }

    if (showCamera) CameraDialog { showCamera = false }
}

@Composable
fun CurrencyPicker(selected: String, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(selected); Icon(Icons.Default.ArrowDropDown, null) }
        DropdownMenu(open, { open = false }) {
            CURRENCIES.forEach { c -> DropdownMenuItem({ Text(c) }, { onSelect(c); open = false }) }
        }
    }
}

// ---------- Cámara (CameraX) ----------
@Composable
fun CameraDialog(onClose: () -> Unit) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(12.dp)) {
                AndroidView(
                    factory = { c ->
                        PreviewView(c).also { pv ->
                            ProcessCameraProvider.getInstance(c).addListener({
                                val provider = ProcessCameraProvider.getInstance(c).get()
                                val preview = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }
                                provider.unbindAll()
                                provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                            }, ContextCompat.getMainExecutor(c))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(16.dp))
                )
                TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) { Text("Cerrar") }
            }
        }
    }
}

// ---------- Micrófono (SpeechRecognizer) ----------
fun granted(ctx: Context, perm: String) =
    ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED

fun listen(ctx: Context, onAmount: (String) -> Unit, onDone: () -> Unit) {
    if (!SpeechRecognizer.isRecognitionAvailable(ctx)) { onDone(); return }
    val sr = SpeechRecognizer.createSpeechRecognizer(ctx)
    sr.setRecognitionListener(object : RecognitionListener {
        override fun onResults(r: Bundle?) {
            val text = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            Regex("""\d+([.,]\d+)?""").find(text)?.value?.replace(',', '.')?.let(onAmount)
            onDone(); sr.destroy()
        }
        override fun onError(e: Int) { onDone(); sr.destroy() }
        override fun onReadyForSpeech(p: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(v: Float) {}
        override fun onBufferReceived(b: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(p: Bundle?) {}
        override fun onEvent(t: Int, p: Bundle?) {}
    })
    sr.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
    })
}
