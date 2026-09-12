package com.example.desafio2dsm

import java.io.Serializable

data class Destino(
    var id: String = "",
    val nombre: String = "",
    val pais: String = "",
    val precio: Double = 0.0,
    val descripcion: String = "",
    val imageUrl: String = ""
) : Serializable