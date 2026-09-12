package com.example.desafio2dsm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.desafio2dsm.databinding.ItemDestinoBinding

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

            Glide.with(ivDestino.context)
                .load(destino.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivDestino)

            btnEdit.setOnClickListener { onEditClick(destino) }
            btnDelete.setOnClickListener { onDeleteClick(destino) }
        }
    }

    override fun getItemCount(): Int = destinos.size

    fun updateList(newDestinos: List<Destino>) {
        destinos = newDestinos
        notifyDataSetChanged()
    }
}