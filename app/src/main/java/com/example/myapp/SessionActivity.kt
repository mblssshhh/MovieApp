package com.example.myapp

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp.models.Movie
import com.example.myapp.models.MovieResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random



class SessionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MovieAdapter
    private var movieList: MutableList<Movie> = mutableListOf()
    private var currentMovieIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        recyclerView = findViewById(R.id.recyclerViewMovies)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MovieAdapter()
        recyclerView.adapter = adapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                loadNextMovie()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        loadMovieList()
    }

    private fun loadMovieList() {
        val apiKey = "3c44bbce"
        val omdbService = OMDbApiClient.getOMDbService()

        for (i in 1..10) {
            val movieTitle = getRandomMovieTitle()
            val call = omdbService.getMovie(movieTitle, apiKey)

            call.enqueue(object : Callback<MovieResponse> {
                override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val movieResponse = response.body()!!
                        val movie = Movie(
                            movieResponse.title,
                            movieResponse.posterUrl,
                            movieResponse.rating,
                            movieResponse.description
                        )
                        movieList.add(movie)

                        // Если список фильмов полностью загружен, начинаем показывать
                        if (movieList.size == 10) {
                            loadNextMovie()
                        }
                    } else {
                        Log.e(TAG, "Failed to load movie data: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                    Log.e(TAG, "Error loading movie: ${t.message}")
                    t.printStackTrace()
                }
            })
        }
    }

    private fun loadNextMovie() {
        if (currentMovieIndex < movieList.size) {
            val movie = movieList[currentMovieIndex]
            adapter.setMovie(movie)
            currentMovieIndex++
        } else {
            Log.d(TAG, "No more movies to show")
            showRetryOrExitDialog()
        }
    }

    private fun showRetryOrExitDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("No more movies to show. Do you want to retry or exit?")
            .setCancelable(false)
            .setPositiveButton("Retry", DialogInterface.OnClickListener { dialog, id ->
                recreate()
            })
            .setNegativeButton("Exit", DialogInterface.OnClickListener { dialog, id ->
                finish()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("No more movies")
        alert.show()
    }

    private fun getRandomMovieTitle(): String {
        val movieTitles = listOf(
            "Inception", "The Dark Knight", "Interstellar", "Fight Club", "The Matrix",
            "Forrest Gump", "Pulp Fiction", "The Shawshank Redemption", "The Godfather",
            "The Lord of the Rings: The Fellowship of the Ring", "The Lord of the Rings: The Two Towers",
            "The Lord of the Rings: The Return of the King", "Star Wars: Episode IV - A New Hope",
            "Star Wars: Episode V - The Empire Strikes Back", "Star Wars: Episode VI - Return of the Jedi",
            "The Avengers", "Avengers: Infinity War", "Avengers: Endgame", "Titanic", "Jurassic Park",
            "The Lion King", "Back to the Future", "Toy Story", "Toy Story 2", "Toy Story 3", "Toy Story 4",
            "Harry Potter and the Sorcerer's Stone", "Harry Potter and the Chamber of Secrets",
            "Harry Potter and the Prisoner of Azkaban", "Harry Potter and the Goblet of Fire",
            "Harry Potter and the Order of the Phoenix", "Harry Potter and the Half-Blood Prince",
            "Harry Potter and the Deathly Hallows: Part 1", "Harry Potter and the Deathly Hallows: Part 2",
            "The Hunger Games", "The Hunger Games: Catching Fire", "The Hunger Games: Mockingjay - Part 1",
            "The Hunger Games: Mockingjay - Part 2", "The Twilight Saga: Twilight", "The Twilight Saga: New Moon",
            "The Twilight Saga: Eclipse", "The Twilight Saga: Breaking Dawn - Part 1", "The Twilight Saga: Breaking Dawn - Part 2",
            "Pirates of the Caribbean: The Curse of the Black Pearl", "Pirates of the Caribbean: Dead Man's Chest",
            "Pirates of the Caribbean: At World's End", "Pirates of the Caribbean: On Stranger Tides",
            "Pirates of the Caribbean: Dead Men Tell No Tales", "The Chronicles of Narnia: The Lion, the Witch and the Wardrobe",
            "The Chronicles of Narnia: Prince Caspian", "The Chronicles of Narnia: The Voyage of the Dawn Treader",
            "Spider-Man", "Spider-Man 2", "Spider-Man 3", "The Amazing Spider-Man", "The Amazing Spider-Man 2",
            "Spider-Man: Homecoming", "Spider-Man: Far From Home", "Spider-Man: No Way Home",
            "Guardians of the Galaxy", "Guardians of the Galaxy Vol. 2", "Guardians of the Galaxy Vol. 3",
            "Iron Man", "Iron Man 2", "Iron Man 3", "Thor", "Thor: The Dark World", "Thor: Ragnarok",
            "Thor: Love and Thunder", "Captain America: The First Avenger", "Captain America: The Winter Soldier",
            "Captain America: Civil War", "Black Panther", "Doctor Strange", "Ant-Man", "Ant-Man and the Wasp",
            "Shang-Chi and the Legend of the Ten Rings", "Eternals", "Black Widow", "Captain Marvel",
            "Wonder Woman", "Wonder Woman 1984", "Man of Steel", "Batman v Superman: Dawn of Justice",
            "Justice League", "Zack Snyder's Justice League", "Aquaman", "The Flash", "Shazam!", "Shazam! Fury of the Gods",
            "Joker", "Birds of Prey", "The Suicide Squad", "Suicide Squad", "Venom", "Venom: Let There Be Carnage",
            "Deadpool", "Deadpool 2", "Logan", "X-Men", "X2: X-Men United", "X-Men: The Last Stand", "X-Men Origins: Wolverine",
            "X-Men: First Class", "X-Men: Days of Future Past", "X-Men: Apocalypse", "Dark Phoenix", "Fantastic Four (2015)", "The Incredibles", "The Incredibles 2",
            "Frozen", "Frozen II", "Moana", "Zootopia", "Ralph Breaks the Internet", "Tangled", "Wreck-It Ralph",
            "Big Hero 6", "Finding Nemo", "Finding Dory", "Monsters, Inc.", "Monsters University", "Coco", "Inside Out",
            "Up", "Brave", "Ratatouille", "Wall-E", "The Good Dinosaur", "Cars", "Cars 2", "Cars 3",
            "A Bug's Life", "Onward", "Soul", "Luca", "Turning Red", "Lightyear", "Encanto", "Raya and the Last Dragon",
            "The Little Mermaid", "Beauty and the Beast", "Aladdin", "The Hunchback of Notre Dame", "Hercules",
            "Mulan", "Tarzan", "The Princess and the Frog", "Tangled", "Wreck-It Ralph", "Frozen", "Frozen II",
            "Moana", "Zootopia", "Ralph Breaks the Internet", "Big Hero 6", "Encanto", "Raya and the Last Dragon",
            "Brave", "Onward", "Soul", "Luca", "Turning Red", "Lightyear"
        )
        return movieTitles.random()
    }

    inner class MovieAdapter : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

        private var movie: Movie? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)
            return MovieViewHolder(view)
        }

        override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
            movie?.let { holder.bind(it) }
        }

        override fun getItemCount(): Int = if (movie != null) 1 else 0

        fun setMovie(movie: Movie) {
            this.movie = movie
            notifyDataSetChanged()
        }

        inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.movieTitleTextView)
            private val posterImageView: ImageView = itemView.findViewById(R.id.moviePosterImageView)
            private val ratingTextView: TextView = itemView.findViewById(R.id.movieRatingTextView)
            private val descriptionTextView: TextView = itemView.findViewById(R.id.movieDescriptionTextView)

            fun bind(movie: Movie) {
                titleTextView.text = movie.title
                ratingTextView.text = movie.rating.toString()
                descriptionTextView.text = movie.description
                Glide.with(itemView.context)
                    .load(movie.posterUrl)
                    .into(posterImageView)
            }
        }
    }

    companion object {
        private const val TAG = "SessionActivity"
    }
}

