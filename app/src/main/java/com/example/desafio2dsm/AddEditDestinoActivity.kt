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
import com.google.firebase.storage.FirebaseStorage
import java.io.Serializable
import java.util.UUID

class AddEditDestinoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditDestinoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private var destinoId: String? = null
    private var existingImageUrl: String? = null

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
        
        // Inicialización automática desde google-services.json
        storage = FirebaseStorage.getInstance()

        setupSpinner()

        val destino = getSerializable(intent, "destino", Destino::class.java)
        if (destino != null) {
            destinoId = destino.id
            existingImageUrl = destino.imageUrl
            binding.tvTitle.text = getString(R.string.title_edit_destination)
            binding.etNombre.setText(destino.nombre)
            binding.etPrecio.setText(destino.precio.toString())
            binding.etDescripcion.setText(destino.descripcion)
            
            val countries = resources.getStringArray(R.array.countries_array)
            val position = countries.indexOf(destino.pais)
            if (position >= 0) {
                binding.spPais.setSelection(position)
            }
            
            Glide.with(this).load(destino.imageUrl).into(binding.ivSelected)
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

        if (imageUri != null) {
            uploadImage(nombre, pais, precio, descripcion)
        } else {
            updateFirestore(nombre, pais, precio, descripcion, existingImageUrl ?: "")
        }
    }

    private fun uploadImage(nombre: String, pais: String, precio: Double, descripcion: String) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        // Aseguramos la ruta correcta: /destinos/archivo.jpg
        val ref = storage.reference.child("destinos").child(fileName)

        imageUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateFirestore(nombre, pais, precio, descripcion, downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("STORAGE_ERROR", "Error: ${e.message}", e)
                    Toast.makeText(this, "Error Storage: ${e.message}. Verifique si Storage está habilitado en la consola.", Toast.LENGTH_LONG).show()
                    binding.btnSave.isEnabled = true
                }
        } ?: run {
            binding.btnSave.isEnabled = true
        }
    }

    private fun updateFirestore(nombre: String, pais: String, precio: Double, descripcion: String, imageUrl: String) {
        val destinoData = hashMapOf(
            "nombre" to nombre,
            "pais" to pais,
            "precio" to precio,
            "descripcion" to descripcion,
            "imageUrl" to imageUrl
        )

        val collection = db.collection("destinos")
        val task = if (destinoId == null) {
            collection.add(destinoData)
        } else {
            collection.document(destinoId!!).set(destinoData)
        }

        task.addOnSuccessListener {
            Toast.makeText(this, "Destino guardado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            binding.btnSave.isEnabled = true
        }
    }
}