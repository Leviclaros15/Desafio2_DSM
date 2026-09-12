package com.example.desafio2dsm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio2dsm.databinding.ItemDestinoBinding
import java.util.Base64

class DestinoAdapter(
    private var destinos: List<Destino>,
    private val onEditClick: (Destino) -> Unit,
    private val onDeleteClick: (Destino) -> Unit
) : RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    class DestinoViewHolder(val binding: ItemDestinoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val binding = ItemDestinoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = destinos[position]
        with(holder.binding) {
            tvNombre.text = destino.nombre
            tvPais.text = destino.pais
            tvPrecio.text = "$${destino.precio}"
            tvDescripcion.text = destino.descripcion

            // Cargar imagen desde Base64
            if (destino.imageData.isNotEmpty()) {
                val bitmap = decodeBase64ToBitmap(destino.imageData)
                if (bitmap != null) {
                    ivDestino.setImageBitmap(bitmap)
                } else {
                    ivDestino.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                ivDestino.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            btnEdit.setOnClickListener { onEditClick(destino) }
            btnDelete.setOnClickListener { onDeleteClick(destino) }
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val byteArray = Base64.getDecoder().decode(base64String)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            null
        }
    }

    override fun getItemCount(): Int = destinos.size

    fun updateList(newDestinos: List<Destino>) {
        destinos = newDestinos
        notifyDataSetChanged()
    }
}