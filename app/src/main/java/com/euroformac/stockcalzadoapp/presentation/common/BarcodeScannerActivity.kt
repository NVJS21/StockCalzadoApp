package com.euroformac.stockcalzadoapp.presentation.common

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.euroformac.stockcalzadoapp.databinding.ActivityBarcodeScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * BarcodeScannerActivity — Componente reutilizable de escaneo de códigos de barras.
 *
 * Uso:
 *   val intent = Intent(this, BarcodeScannerActivity::class.java)
 *   barcodeLauncher.launch(intent)
 *
 * Devuelve:
 *   - RESULT_OK  + Intent con extra EXTRA_BARCODE_VALUE (String) si se detectó un código.
 *   - RESULT_CANCELED si el usuario canceló o hubo error.
 */
class BarcodeScannerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BARCODE_VALUE = "extra_barcode_value"
        private const val TAG = "BarcodeScanner"
    }

    private lateinit var binding: ActivityBarcodeScannerBinding
    private lateinit var cameraExecutor: ExecutorService

    /** Previene múltiples devoluciones por el mismo frame */
    private val codeDetected = AtomicBoolean(false)

    // ── Permiso de cámara ─────────────────────────────────────────────────────
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnCancelarEscaneo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        startScanLineAnimation()
        checkCameraPermission()
    }

    // ── Permiso ───────────────────────────────────────────────────────────────
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ── CameraX ───────────────────────────────────────────────────────────────
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { rawValue ->
                            onBarcodeDetected(rawValue)
                        })
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al abrir la cámara", e)
                runOnUiThread {
                    Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // ── Código detectado ──────────────────────────────────────────────────────
    private fun onBarcodeDetected(rawValue: String) {
        // Garantizar una sola devolución
        if (!codeDetected.compareAndSet(false, true)) return

        runOnUiThread {
            binding.tvScanStatus.text = "✓ Código detectado"
            binding.tvScanStatus.setTextColor(android.graphics.Color.parseColor("#00FF88"))
        }

        val result = Intent().apply {
            putExtra(EXTRA_BARCODE_VALUE, rawValue)
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    // ── Animación de la línea de escaneo ──────────────────────────────────────
    private fun startScanLineAnimation() {
        val anim = TranslateAnimation(0f, 0f, -200f, 200f).apply {
            duration = 1500
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.scanLine.startAnimation(anim)
    }

    // ── Ciclo de vida ─────────────────────────────────────────────────────────
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // ── Analizador de imágenes ────────────────────────────────────────────────
    private class BarcodeAnalyzer(
        private val onDetected: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val scanner = BarcodeScanning.getClient()

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val raw = barcode.rawValue ?: continue
                        if (raw.isNotBlank()) {
                            onDetected(raw)
                            break
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("BarcodeAnalyzer", "Error analizando frame", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
