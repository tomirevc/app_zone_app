package com.example.app_s10

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GamesListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameAdapter
    private lateinit var progressBar: ProgressBar

    private val gamesList = mutableListOf<Game>()

    companion object {
        private const val TAG = "GamesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerViewGames)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GameAdapter(gamesList)
        recyclerView.adapter = adapter

        loadGames()
    }

    private fun loadGames() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        database.child("games").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    gamesList.clear()
                    for (gameSnapshot in snapshot.children) {
                        val game = gameSnapshot.getValue(Game::class.java)
                        game?.let { gamesList.add(it) }
                    }
                    adapter.updateGames(gamesList)
                    progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al leer juegos: ${error.message}")
                    Toast.makeText(this@GamesListActivity, "Error al cargar juegos", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            })
    }
}
