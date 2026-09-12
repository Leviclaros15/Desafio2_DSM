package com.example.desafio2dsm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafio2dsm.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: DestinoAdapter
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditDestinoActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = DestinoAdapter(
            emptyList(),
            onEditClick = { destino ->
                val intent = Intent(this, AddEditDestinoActivity::class.java)
                intent.putExtra("destino", destino)
                startActivity(intent)
            },
            onDeleteClick = { destino ->
                showDeleteConfirmation(destino)
            }
        )
        binding.rvDestinos.layoutManager = LinearLayoutManager(this)
        binding.rvDestinos.adapter = adapter
    }

    private fun showDeleteConfirmation(destino: Destino) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_delete))
            .setMessage(getString(R.string.msg_confirm_delete))
            .setPositiveButton("Eliminar") { _, _ ->
                deleteDestino(destino)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteDestino(destino: Destino) {
        db.collection("destinos").document(destino.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Destino eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        listenForDestinos()
    }

    override fun onStop() {
        super.onStop()
        firestoreListener?.remove()
    }

    private fun listenForDestinos() {
        firestoreListener = db.collection("destinos")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val list = mutableListOf<Destino>()
                for (doc in snapshots!!) {
                    val destino = doc.toObject(Destino::class.java)
                    destino.id = doc.id
                    list.add(destino)
                }
                adapter.updateList(list)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}