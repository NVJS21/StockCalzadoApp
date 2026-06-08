package com.euroformac.stockcalzadoapp.presentation.almacen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.euroformac.stockcalzadoapp.databinding.FragmentAlmacenBinding
import com.euroformac.stockcalzadoapp.presentation.common.BarcodeScannerActivity
import dagger.hilt.android.AndroidEntryPoint
import com.euroformac.stockcalzadoapp.R
import com.euroformac.stockcalzadoapp.domain.model.PedidoAlmacen
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlmacenFragment : Fragment() {

    private var _binding: FragmentAlmacenBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlmacenViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var ivPreview: android.widget.ImageView? = null
    private var tempPhotoUri: Uri? = null

    // ── Escáner: pedido activo que está siendo confirmado ──────────────────────
    private var pedidoParaConfirmar: PedidoAlmacen? = null

    // ── Escáner: referencia al campo de código en los diálogos ────────────────
    private var etCodigoParaRellenar: EditText? = null

    // ── Launchers de imagen existentes ────────────────────────────────────────
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            ivPreview?.setImageURI(it)
            ivPreview?.visibility = View.VISIBLE
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri = tempPhotoUri
            ivPreview?.setImageURI(tempPhotoUri)
            ivPreview?.visibility = View.VISIBLE
        } else {
            // El usuario canceló o la cámara falló — limpiar URI temporal
            tempPhotoUri = null
            // Solo mostrar mensaje si hubo un intento real (no cancelación limpia)
            // No crashear en ningún caso
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }
            else -> {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun abrirCamara() {
        try {
            val uri = createTempPhotoUri()
            if (uri != null) {
                tempPhotoUri = uri
                takePhotoLauncher.launch(uri)
            } else {
                Toast.makeText(requireContext(), "No se pudo preparar el archivo de foto", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al abrir la cámara: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ── Launcher escáner: confirmar pedido pendiente ───────────────────────────
    private val scannerPedidoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val codigoEscaneado = result.data
                ?.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE_VALUE)
                ?.trim() ?: ""
            val pedido = pedidoParaConfirmar
            if (pedido != null && codigoEscaneado.isNotBlank()) {
                if (codigoEscaneado == pedido.codigoBarras) {
                    viewModel.completarPedidoConStock(pedido) { success, message ->
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "El código no coincide", Toast.LENGTH_SHORT).show()
                }
            }
        }
        pedidoParaConfirmar = null
    }

    // ── Launcher escáner: rellenar campo de código (opción A y B) ─────────────
    private val scannerCodigoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val codigoEscaneado = result.data
                ?.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE_VALUE)
                ?.trim() ?: ""
            if (codigoEscaneado.isNotBlank()) {
                etCodigoParaRellenar?.setText(codigoEscaneado)
                Toast.makeText(requireContext(), "Código añadido al formulario", Toast.LENGTH_SHORT).show()
            }
        }
        etCodigoParaRellenar = null
    }

    private val adapter = ProductoAdapter(
        onVerTallasClick = { producto ->
            viewLifecycleOwner.lifecycleScope.launch {
                val tallas = viewModel.getTallas(producto.id)
                
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ver_tallas, null)
                val tvTitulo = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_titulo)
                val tvInfo = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_color_precio)
                val container = dialogView.findViewById<android.widget.LinearLayout>(R.id.layout_tallas_container)

                tvTitulo.text = "${producto.marca} ${producto.modelo}"
                tvInfo.text = "Color: ${producto.color}  ·  Precio: ${"%.2f".format(producto.precio)}€"

                if (tallas.isEmpty()) {
                    val emptyTv = android.widget.TextView(requireContext()).apply {
                        text = "No hay tallas registradas."
                        setTextColor(android.graphics.Color.GRAY)
                        setPadding(0, 20, 0, 0)
                    }
                    container.addView(emptyTv)
                } else {
                    tallas.forEach { talla ->
                        val rowView = LayoutInflater.from(requireContext()).inflate(R.layout.item_talla_fila, container, false)
                        val tvTalla = rowView.findViewById<android.widget.TextView>(R.id.tv_fila_talla)
                        val tvStock = rowView.findViewById<android.widget.TextView>(R.id.tv_fila_stock)
                        val tvCodigo = rowView.findViewById<android.widget.TextView>(R.id.tv_fila_codigo)

                        val tallaStr = if (talla.talla % 1.0 == 0.0) talla.talla.toInt().toString() else talla.talla.toString()
                        tvTalla.text = "Talla $tallaStr"
                        tvStock.text = "${talla.stockAlmacen} uds"
                        tvCodigo.text = if (talla.codigoBarras.isNullOrBlank()) "Código pendiente" else talla.codigoBarras
                        
                        container.addView(rowView)
                    }
                }

                android.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
        },
        onAumentarStockClick = { producto ->
            showAjusteManualDialog(producto, true)
        },
        onDisminuirStockClick = { producto ->
            showAjusteManualDialog(producto, false)
        }
    )

    private fun showAjusteManualDialog(producto: com.euroformac.stockcalzadoapp.domain.model.Producto, esAumento: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val tallas = viewModel.getTallas(producto.id)
            if (tallas.isEmpty()) {
                Toast.makeText(requireContext(), "No hay tallas para este producto", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val tallasArray = tallas.map { "Talla ${it.talla} (Stock: ${it.stockAlmacen})" }.toTypedArray()
            var seleccionada = 0

            val layout = android.widget.LinearLayout(requireContext())
            layout.orientation = android.widget.LinearLayout.VERTICAL
            layout.setPadding(50, 20, 50, 0)

            val cantInput = EditText(requireContext())
            cantInput.hint = "Cantidad a ${if (esAumento) "sumar" else "restar"}"
            cantInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            cantInput.setText("1")
            layout.addView(cantInput)

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Ajuste Manual: ${if (esAumento) "+" else "-"}")
                .setSingleChoiceItems(tallasArray, 0) { _, which -> seleccionada = which }
                .setView(layout)
                .setPositiveButton("Confirmar") { _, _ ->
                    val cantidad = cantInput.text.toString().toIntOrNull() ?: 0
                    if (cantidad > 0) {
                        val talla = tallas[seleccionada]
                        if (!esAumento && talla.stockAlmacen < cantidad) {
                            Toast.makeText(requireContext(), "Stock insuficiente", Toast.LENGTH_SHORT).show()
                        } else {
                            val nuevaCantidad = if (esAumento) talla.stockAlmacen + cantidad else talla.stockAlmacen - cantidad
                            viewModel.aumentarStockAlmacen(talla.copy(stockAlmacen = nuevaCantidad))
                            Toast.makeText(requireContext(), "Stock actualizado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlmacenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        
        binding.btnRegistrarEntrada.setOnClickListener {
            val options = arrayOf("A) Añadir stock a producto existente", "B) Registrar nuevo producto")
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("+ ENTRADA")
                .setItems(options) { _, which ->
                    if (which == 0) showDialogOpcionA() else showDialogOpcionB()
                }
                .show()
        }

        binding.btnEliminar.setOnClickListener {
            showDeleteDialog()
        }

        binding.fabPedidosPendientes.setOnClickListener {
            showPedidosPendientesDialog()
        }
    }

    private fun showDeleteDialog() {
        val options = arrayOf("Eliminar talla específica", "Eliminar producto completo")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("ELIMINAR")
            .setItems(options) { _, which ->
                if (which == 0) showDeleteTallaDialog() else showDeleteProductoDialog()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteTallaDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val productos = viewModel.productos.value
            if (productos.isEmpty()) {
                Toast.makeText(requireContext(), "No hay productos", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val productNames = productos.map { "${it.marca} ${it.modelo} (${it.color})" }.toTypedArray()
            
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar Producto")
                .setItems(productNames) { _, pIndex ->
                    val producto = productos[pIndex]
                    viewLifecycleOwner.lifecycleScope.launch {
                        val tallas = viewModel.getTallas(producto.id)
                        if (tallas.isEmpty()) {
                            Toast.makeText(requireContext(), "No hay tallas para este producto", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val tallaNames = tallas.map { "Talla ${it.talla}" }.toTypedArray()
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Seleccionar Talla")
                            .setItems(tallaNames) { _, tIndex ->
                                val talla = tallas[tIndex]
                                val tallaStr = if (talla.talla % 1.0 == 0.0) talla.talla.toInt().toString() else talla.talla.toString()
                                
                                val confirmAction = {
                                    android.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Confirmar eliminación")
                                        .setMessage("¿Seguro que quieres eliminar la talla $tallaStr de ${producto.modelo}?")
                                        .setPositiveButton("Eliminar") { _, _ ->
                                            viewModel.eliminarTalla(producto.id, talla.talla, 
                                                onConfirmExtra = { count, onOk ->
                                                    android.app.AlertDialog.Builder(requireContext())
                                                        .setTitle("¡Advertencia!")
                                                        .setMessage("Hay $count pedidos PENDIENTES asociados a esta talla. ¿Eliminar de todos modos?")
                                                        .setPositiveButton("Sí, eliminar", { _, _ -> onOk() })
                                                        .setNegativeButton("Cancelar", null)
                                                        .show()
                                                },
                                                onResult = { success, msg ->
                                                    requireActivity().runOnUiThread {
                                                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                        .setNegativeButton("Cancelar", null)
                                        .show()
                                    Unit
                                }
                                confirmAction()
                            }
                            .show()
                    }
                }
                .show()
        }
    }

    private fun showDeleteProductoDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val productos = viewModel.productos.value
            if (productos.isEmpty()) {
                Toast.makeText(requireContext(), "No hay productos", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val productNames = productos.map { "${it.marca} ${it.modelo} (${it.color})" }.toTypedArray()
            
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar Producto Completo")
                .setItems(productNames) { _, pIndex ->
                    val producto = productos[pIndex]
                    
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Confirmación FUERTE")
                        .setMessage("¿Seguro que quieres eliminar ${producto.modelo} y TODAS sus tallas? Esta acción no se puede deshacer.")
                        .setPositiveButton("ELIMINAR TODO") { _, _ ->
                            viewModel.eliminarProducto(producto.id, 
                                onConfirmExtra = { count, onOk ->
                                    android.app.AlertDialog.Builder(requireContext())
                                        .setTitle("¡ATENCIÓN!")
                                        .setMessage("Hay $count pedidos PENDIENTES asociados a este producto. ¿Eliminar TODO de todos modos?")
                                        .setPositiveButton("SÍ, ELIMINAR TODO", { _, _ -> onOk() })
                                        .setNegativeButton("Cancelar", null)
                                        .show()
                                },
                                onResult = { success, msg ->
                                    requireActivity().runOnUiThread {
                                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                .show()
        }
    }

    private fun showDialogOpcionA() {
        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val codeInput = EditText(requireContext()).apply {
            hint = "Código de barras"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val btnScanA = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "📷  Escanear código"
            setOnClickListener {
                etCodigoParaRellenar = codeInput
                try {
                    val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                    scannerCodigoLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val cantInput = EditText(requireContext()).apply {
            hint = "Cantidad (ej: 1)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("1")
        }

        layout.addView(btnScanA)
        layout.addView(codeInput)
        layout.addView(cantInput)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Añadir Stock (Existente)")
            .setView(layout)
            .setPositiveButton("Confirmar") { _, _ ->
                val codigo = codeInput.text.toString()
                val cantidad = cantInput.text.toString().toIntOrNull() ?: 0
                if (codigo.isNotBlank() && cantidad > 0) {
                    viewModel.registrarEntradaConCodigo(codigo, cantidad) { success, message ->
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            if (!success) {
                                android.app.AlertDialog.Builder(requireContext())
                                    .setMessage("¿Deseas registrar este nuevo producto?")
                                    .setPositiveButton("Sí") { _, _ -> showDialogOpcionB(codigo) }
                                    .setNegativeButton("No", null)
                                    .show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDialogOpcionB(prefilledCode: String = "") {
        val scrollView = android.widget.ScrollView(requireContext())
        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val etNombre = EditText(requireContext()).apply { hint = "Nombre/Modelo" }
        val etMarca = EditText(requireContext()).apply { hint = "Marca" }
        val etColor = EditText(requireContext()).apply { hint = "Color" }
        val etGenero = EditText(requireContext()).apply { hint = "Género" }
        val etPrecio = EditText(requireContext()).apply { hint = "Precio"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etTallaInicial = EditText(requireContext()).apply { hint = "Talla Inicial (ej: 38)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etTallaFinal = EditText(requireContext()).apply { hint = "Talla Final (ej: 44)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val etCodigo = EditText(requireContext()).apply { 
            hint = "Código de barras"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefilledCode)
        }
        val etStock = EditText(requireContext()).apply { hint = "Stock Inicial (1ª talla)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER; setText("1") }

        // --- Gestión de Imagen ---
        val tvImgLabel = android.widget.TextView(requireContext()).apply {
            text = "Imagen del producto:"
            setPadding(0, 30, 0, 10)
            setTextColor(android.graphics.Color.WHITE)
        }
        
        ivPreview = android.widget.ImageView(requireContext()).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            visibility = View.GONE
            setBackgroundColor(android.graphics.Color.DKGRAY)
        }
        selectedImageUri = null // Reset

        val btnGaleria = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Seleccionar de Galería"
            setIconResource(android.R.drawable.ic_menu_gallery)
            setOnClickListener { pickImageLauncher.launch("image/*") }
        }

        val btnCamara = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Hacer Foto"
            setIconResource(android.R.drawable.ic_menu_camera)
            setOnClickListener {
                checkCameraPermissionAndOpen()
            }
        }

        val btnScanCodigo = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "📷  Escanear código base"
            setOnClickListener {
                etCodigoParaRellenar = etCodigo
                try {
                    val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                    scannerCodigoLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
                }
            }
        }

        layout.addView(etNombre)
        layout.addView(etMarca)
        layout.addView(etColor)
        layout.addView(etGenero)
        layout.addView(etPrecio)
        layout.addView(etTallaInicial)
        layout.addView(etTallaFinal)
        layout.addView(btnScanCodigo)
        layout.addView(etCodigo)
        layout.addView(etStock)
        layout.addView(tvImgLabel)
        layout.addView(btnGaleria)
        layout.addView(btnCamara)
        layout.addView(ivPreview)
        scrollView.addView(layout)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Registrar Nuevo Producto con Rango")
            .setView(scrollView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString()
                val marca = etMarca.text.toString()
                val color = etColor.text.toString()
                val genero = etGenero.text.toString()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val tallaIn = etTallaInicial.text.toString().toDoubleOrNull() ?: 0.0
                val tallaFin = etTallaFinal.text.toString().toDoubleOrNull() ?: 0.0
                val codigo = etCodigo.text.toString()
                val stock = etStock.text.toString().toIntOrNull() ?: 0

                val finalImgUrl = selectedImageUri?.let { copyImageToInternalStorage(it) } ?: "producto_placeholder"

                if (nombre.isNotBlank() && tallaIn > 0 && tallaFin >= tallaIn && codigo.isNotBlank()) {
                    viewModel.registrarProductoConRangoTallas(
                        nombre, marca, color, genero, precio, tallaIn, tallaFin, codigo, stock, finalImgUrl
                    ) { success, message ->
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorMsg = if (tallaFin < tallaIn) "La talla final debe ser mayor o igual" else "Faltan campos obligatorios"
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPedidosPendientesDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pedidos, null)
        val tabLayout = dialogView.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_layout_pedidos)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_pedidos_dialog)

        val pedidoAdapter = PedidoAdapter(
            onPedidoClick = { pedido ->
                showGestionPedidoDialog(pedido)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = pedidoAdapter

        // Observar pedidos y filtrar según pestaña seleccionada
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todosLosPedidos.collectLatest { todos ->
                fun actualizarLista(tabIndex: Int) {
                    val filtrados = when (tabIndex) {
                        0 -> todos.filter { it.estado == "PENDIENTE" }
                        1 -> todos.filter { it.estado == "COMPLETADO" }
                        2 -> todos.filter { it.estado == "FALLIDO" }
                        else -> emptyList()
                    }
                    pedidoAdapter.submitList(filtrados)

                    val tvEmpty = dialogView.findViewById<android.widget.TextView>(R.id.tv_empty_pedidos)
                    if (filtrados.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = when(tabIndex) {
                            0 -> "No hay pedidos pendientes"
                            1 -> "No hay pedidos completados"
                            2 -> "No hay pedidos fallidos"
                            else -> ""
                        }
                    } else {
                        tvEmpty.visibility = View.GONE
                    }
                }

                actualizarLista(tabLayout.selectedTabPosition)

                tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                        actualizarLista(tab.position)
                    }
                    override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
                    override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
                })
            }
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Gestión de Pedidos")
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun showGestionPedidoDialog(pedido: PedidoAlmacen) {
        // Layout con botón escanear + campo manual
        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 10)

        val tvCodigo = android.widget.TextView(requireContext()).apply {
            text = "Código esperado: ${pedido.codigoBarras}"
            setTextColor(android.graphics.Color.LTGRAY)
            textSize = 12f
            setPadding(0, 0, 0, 12)
        }

        val btnEscanear = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "📷  Escanear código"
            setOnClickListener {
                pedidoParaConfirmar = pedido
                try {
                    val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                    scannerPedidoLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val tvManual = android.widget.TextView(requireContext()).apply {
            text = "— o introduce manualmente —"
            setTextColor(android.graphics.Color.GRAY)
            textSize = 12f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 16, 0, 8)
        }

        val input = EditText(requireContext()).apply {
            hint = "Introduce código de barras"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(tvCodigo)
        layout.addView(btnEscanear)
        layout.addView(tvManual)
        layout.addView(input)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Pedido")
            .setMessage("Producto: ${pedido.nombreProducto}")
            .setView(layout)
            .setPositiveButton("Confirmar") { _, _ ->
                val codigo = input.text.toString()
                if (codigo == pedido.codigoBarras) {
                    viewModel.completarPedidoConStock(pedido) { success, message ->
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "El código no coincide", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Marcar como fallido") { _, _ ->
                viewModel.marcarPedidoComoFallido(pedido.id)
                Toast.makeText(requireContext(), "Pedido marcado como FALLIDO ✕", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupRecyclerView() {
        binding.rvProductos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProductos.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observar loading
            viewModel.isLoading.collectLatest { loading ->
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observar pedidos pendientes para el badge
            viewModel.pedidosPendientes.collectLatest { pedidos ->
                val count = pedidos.size
                if (count > 0) {
                    binding.badgePedidos.text = count.toString()
                    binding.badgePedidos.visibility = View.VISIBLE
                } else {
                    binding.badgePedidos.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observar la lista de productos
            viewModel.productos.collectLatest { productos ->
                if (productos.isEmpty() && !viewModel.isLoading.value) {
                    binding.rvProductos.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvProductos.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE

                    // Crear una lista de Flows, uno por cada producto, que emite ProductoConStock
                    val productFlows = productos.map { producto ->
                        viewModel.getStockTotalFlow(producto.id).map { stockTotal ->
                            ProductoConStock(producto, stockTotal)
                        }
                    }

                    if (productFlows.isNotEmpty()) {
                        // Combinar todos los flows en uno solo que emite la lista completa actualizada
                        combine(productFlows) { it.toList() }.collect { itemsConStock ->
                            adapter.submitList(itemsConStock)
                        }
                    } else {
                        adapter.submitList(emptyList())
                    }
                }
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val fileName = "prod_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createTempPhotoUri(): Uri? {
        return try {
            val directory = File(requireContext().filesDir, "product_images")
            if (!directory.exists()) directory.mkdirs()
            
            val tempFile = File(directory, "product_${System.currentTimeMillis()}.jpg")
            // No es estrictamente necesario crear el archivo antes de TakePicture, 
            // pero nos asegura que la ruta es válida.
            
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
