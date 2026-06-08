package com.euroformac.stockcalzadoapp.presentation.tienda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.euroformac.stockcalzadoapp.databinding.FragmentTiendaBinding
import com.euroformac.stockcalzadoapp.presentation.common.BarcodeScannerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TiendaFragment : Fragment() {

    private var _binding: FragmentTiendaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TiendaViewModel by viewModels()

    /** Launcher para el escáner de Tienda */
    private lateinit var scannerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar launcher ANTES de onCreateView (obligatorio en Fragments)
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val codigo = result.data
                    ?.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE_VALUE)
                    ?.trim()
                if (!codigo.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Código detectado: $codigo", Toast.LENGTH_SHORT).show()
                    viewModel.crearSolicitudPedido(codigo)
                    Toast.makeText(requireContext(), "Solicitud enviada a almacén", Toast.LENGTH_SHORT).show()
                }
            }
            // Si RESULT_CANCELED: el usuario canceló o falló la cámara → no hacer nada
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTiendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón grande → abrir cámara
        binding.btnEscanearModelo.setOnClickListener {
            abrirEscaner()
        }

        // Botón pequeño manual → diálogo manual (backup)
        binding.btnIntroduccionManual.setOnClickListener {
            showManualEntryDialog()
        }
    }

    /** Abre BarcodeScannerActivity. Si falla, muestra Toast pero NO cierra la app. */
    private fun abrirEscaner() {
        try {
            val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
            scannerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showManualEntryDialog() {
        val input = EditText(requireContext())
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Introducción Manual")
            .setMessage("Introduce el código de barras:")
            .setView(input)
            .setPositiveButton("Confirmar") { _, _ ->
                val codigo = input.text.toString()
                if (codigo.isNotBlank()) {
                    Toast.makeText(requireContext(), "Código introducido: $codigo", Toast.LENGTH_SHORT).show()
                    viewModel.crearSolicitudPedido(codigo)
                    Toast.makeText(requireContext(), "Solicitud enviada a almacén", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
