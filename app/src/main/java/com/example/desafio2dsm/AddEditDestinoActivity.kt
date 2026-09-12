package com.example.desafio2dsm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.desafio2dsm.databinding.ActivityAddEditDestinoBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.Base64

class AddEditDestinoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditDestinoBinding
    private lateinit var db: FirebaseFirestore
    private var imageUri: Uri? = null
    private var destinoId: String? = null
    private var existingImageData: String? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.ivSelected.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditDestinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupSpinner()

        val destino = getSerializable(intent, "destino", Destino::class.java)
        if (destino != null) {
            destinoId = destino.id
            existingImageData = destino.imageData
            binding.tvTitle.text = getString(R.string.title_edit_destination)
            binding.etNombre.setText(destino.nombre)
            binding.etPrecio.setText(destino.precio.toString())
            binding.etDescripcion.setText(destino.descripcion)
            
            val countries = resources.getStringArray(R.array.countries_array)
            val position = countries.indexOf(destino.pais)
            if (position >= 0) {
                binding.spPais.setSelection(position)
            }
            
            // Cargar imagen desde Base64
            if (destino.imageData.isNotEmpty()) {
                val bitmap = decodeBase64ToBitmap(destino.imageData)
                if (bitmap != null) {
                    binding.ivSelected.setImageBitmap(bitmap)
                }
            }
        }

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectImageLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            saveDestino()
        }
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.countries_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spPais.adapter = adapter
        }
    }

    private fun <T : Serializable?> getSerializable(intent: Intent, key: String, m_class: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra(key, m_class)
        else
            @Suppress("DEPRECATION") intent.getSerializableExtra(key) as? T
    }

    private fun saveDestino() {
        val nombre = binding.etNombre.text.toString().trim()
        val pais = binding.spPais.selectedItem.toString()
        val precioStr = binding.etPrecio.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val countries = resources.getStringArray(R.array.countries_array)

        if (nombre.isEmpty() || pais == countries[0] || precioStr.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, getString(R.string.err_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val precio = precioStr.toDoubleOrNull() ?: 0.0
        if (precio <= 0) {
            Toast.makeText(this, getString(R.string.err_invalid_price), Toast.LENGTH_SHORT).show()
            return
        }

        if (descripcion.length < 20) {
            Toast.makeText(this, getString(R.string.err_short_desc), Toast.LENGTH_SHORT).show()
            return
        }

        if (destinoId == null && imageUri == null) {
            Toast.makeText(this, getString(R.string.err_no_image), Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false

        Log.d("SAVE_DESTINO", "destinoId: $destinoId, imageUri: $imageUri, existingImageData: $existingImageData")

        if (imageUri != null) {
            val base64String = convertImageToBase64(imageUri!!)
            if (base64String != null) {
                updateFirestore(nombre, pais, precio, descripcion, base64String)
            } else {
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
        } else {
            if (destinoId != null && (existingImageData == null || existingImageData!!.isEmpty())) {
                Toast.makeText(this, "Error: La imagen original no existe. Por favor selecciona una nueva imagen.", Toast.LENGTH_LONG).show()
                binding.btnSave.isEnabled = true
                return
            }
            updateFirestore(nombre, pais, precio, descripcion, existingImageData ?: "")
        }
    }

    private fun convertImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Reducir tamaño para no exceder límite de Firestore (1MB)
            val maxSize = 500 // px
            val width = bitmap.width
            val height = bitmap.height
            
            val scale = if (width > height) {
                maxSize.toFloat() / width
            } else {
                maxSize.toFloat() / height
            }
            
            val scaledWidth = (width * scale).toInt()
            val scaledHeight = (height * scale).toInt()
            
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
            
            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            
            Base64.getEncoder().encodeToString(byteArray)
        } catch (e: Exception) {
            Log.e("BASE64", "Error convirtiendo imagen: ${e.message}", e)
            null
        }
    }
    
    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val byteArray = Base64.getDecoder().decode(base64String)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            Log.e("BASE64", "Error decodificando imagen: ${e.message}", e)
            null
        }
    }

    private fun updateFirestore(nombre: String, pais: String, precio: Double, descripcion: String, imageData: String) {
        val destinoData = hashMapOf(
            "nombre" to nombre,
            "pais" to pais,
            "precio" to precio,
            "descripcion" to descripcion,
            "imageData" to imageData
        )

        val collection = db.collection("destinos")
        val task = if (destinoId == null) {
            collection.add(destinoData)
        } else {
            collection.document(destinoId!!).set(destinoData)
        }

        task.addOnSuccessListener {
            Toast.makeText(this, "Destino guardado con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            binding.btnSave.isEnabled = true
        }
    }
}