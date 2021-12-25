package com.aemerse.muserse.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.snackbar.Snackbar
import com.aemerse.muserse.ApplicationClass
import com.aemerse.muserse.R
import com.aemerse.muserse.uiElementHelper.ColorHelper
import com.aemerse.muserse.databinding.ActivitySavedLyricsBinding
import com.aemerse.muserse.model.Constants
import com.aemerse.muserse.qlyrics.lyrics.Lyrics
import com.aemerse.muserse.qlyrics.offlineStorage.OfflineStorageArtistBio
import com.aemerse.muserse.qlyrics.offlineStorage.OfflineStorageLyrics
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.Serializable
import java.util.*
import java.util.concurrent.Executors

class ActivitySavedLyrics: AppCompatActivity() {

    private lateinit var mSearchAction: MenuItem
    private var isSearchOpened = false
    private var imm: InputMethodManager? = null
    private lateinit var editSearch: EditText

    val adapter = SavedLyricsAdapter()
    var artistImageUrls: HashMap<String, String> = hashMapOf()
    val handler = Handler(Looper.getMainLooper())
    private lateinit var binding: ActivitySavedLyricsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySavedLyricsBinding.inflate(layoutInflater)
        ColorHelper.setStatusBarGradiant(this)

        when (ApplicationClass.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_lyrics)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        binding.recyclerViewSavedLyrics.adapter = adapter
        binding.recyclerViewSavedLyrics.layoutManager = LinearLayoutManager(this)

        title = getString(R.string.nav_saved_lyrics)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        Executors.newSingleThreadExecutor().execute {
            //load urls and lyrics in thread and update UI later
            artistImageUrls = OfflineStorageArtistBio.getArtistImageUrls()
            adapter.setLyrics(OfflineStorageLyrics.getAllSavedLyrics())
            handler.post {
                binding.progressBarSavedLyrics.visibility =View.GONE
                if(adapter.isEmpty()){
                    binding.emptyLyrics.visibility = View.VISIBLE
                }else{
                    binding.recyclerViewSavedLyrics.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_saved_lyrics, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        mSearchAction = menu?.findItem(R.id.action_search)!!
        if (isSearchOpened) {
            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp)
        } else {
            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_white_48dp)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_search -> {handleSearch()}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSearch() {
        if (isSearchOpened) { //test if the search is open
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayShowCustomEnabled(false)
                supportActionBar!!.setDisplayShowTitleEnabled(true)
            }

            //hides the keyboard
            var view = currentFocus
            if (view == null) {
                view = View(this)
            }
            imm?.hideSoftInputFromWindow(view.windowToken, 0)

            //add the search icon in the action bar
            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_white_48dp)
            adapter.filter("")

            isSearchOpened = false
        } else { //open the search entry

            if (supportActionBar != null) {
                supportActionBar!!.setDisplayShowCustomEnabled(true) //enable it to display a custom view
                supportActionBar!!.setCustomView(R.layout.search_bar_layout)//add the custom view
                supportActionBar!!.setDisplayShowTitleEnabled(false) //hide the title
            }
            editSearch = supportActionBar!!.customView.findViewById(R.id.edtSearch) //the text editor
            editSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    // TODO Auto-generated method stub
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // TODO Auto-generated method stub
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    Log.d("Search", s.toString())
                    adapter.filter(s.toString())
                }
            })
            editSearch.setOnClickListener { imm?.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT) }

            editSearch.requestFocus()

            //open the keyboard focused in the edtSearch
            imm?.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT)

            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp)
            //add the close icon
            //mSearchAction.setIcon(getResources().getDrawable(R.drawable.cancel));
            isSearchOpened = true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    inner class SavedLyricsAdapter: RecyclerView.Adapter<SavedLyricsAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@ActivitySavedLyrics).inflate(R.layout.item_saved_lyric, parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.trackInfo.text = lyrics[position].getTrack()
            holder.playCount.text = lyrics[position].getArtist()
            holder.delete.isEnabled = true
            Glide.with(this@ActivitySavedLyrics)
                .load(artistImageUrls[lyrics[position].getOriginalArtist()])
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(withCrossFade())
                .into(holder.imageView)
        }

        private var lyrics: MutableList<Lyrics> = mutableListOf()
        private var copyLyrics: MutableList<Lyrics> = mutableListOf()

        fun setLyrics(lyrics: MutableList<Lyrics>){
            this.lyrics = lyrics
            copyLyrics.addAll(lyrics)
        }

        fun isEmpty(): Boolean{
            return lyrics.isEmpty()
        }

        override fun getItemCount(): Int {
            return lyrics.size
        }

        fun filter(keyword: String){
            lyrics.clear()
            when {
                keyword.isEmpty() -> {
                    lyrics.addAll(copyLyrics)
                }
                else -> {
                    copyLyrics.forEach { lyric ->
                        if(lyric.getTrack()!!.contains(keyword, true) || lyric.getArtist()!!.contains(keyword, true))
                            lyrics.add(lyric)
                    }
                }
            }
            notifyDataSetChanged()
        }

        inner class MyViewHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {
            val trackInfo: TextView = findViewById(R.id.trackInfo)
            val playCount: TextView = findViewById(R.id.playCount)
            val imageView: ImageView = findViewById(R.id.imageView)
            val delete: ImageView = findViewById(R.id.delete)

            init {
                v.setOnClickListener(this)
                delete.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                val position = adapterPosition  //adapter position changes sometimes in between, don't know why
                when(v?.id){
                    R.id.root_view_item_saved_lyrics -> {
                        val intent = Intent(this@ActivitySavedLyrics, ActivityLyricView::class.java)
                        intent.putExtra("track_title", lyrics[position].getOriginalTrack())
                        intent.putExtra("artist", lyrics[position].getOriginalArtist())
                        intent.putExtra("lyrics", lyrics[position] as Serializable)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    R.id.delete ->{
                        v.isEnabled = false  //to prevent double clicks
                        when {
                            OfflineStorageLyrics.clearLyricsFromDB(lyrics[position].getOriginalTrack()!!, lyrics[position].getTrackId()) -> {
                                Snackbar.make(v, getString(R.string.lyrics_removed), Snackbar.LENGTH_SHORT).show()
                                lyrics.removeAt(position)
                                notifyItemRemoved(position)
                            }
                            else -> {
                                v.isEnabled = true //enable click again
                                Snackbar.make(v, getString(R.string.error_removing), Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

    }

}