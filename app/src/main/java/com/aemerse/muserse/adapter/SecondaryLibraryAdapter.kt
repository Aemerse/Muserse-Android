package com.aemerse.muserse.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.aemerse.muserse.ApplicationClass
import com.aemerse.muserse.R
import com.aemerse.muserse.uiElementHelper.ColorHelper
import com.aemerse.muserse.uiElementHelper.TypeFaceHelper
import com.aemerse.muserse.activity.ActivityTagEditor
import com.aemerse.muserse.model.*
import com.aemerse.muserse.service.PlayerService
import com.aemerse.muserse.utils.UtilityFun
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SecondaryLibraryAdapter : RecyclerView.Adapter<SecondaryLibraryAdapter.MyViewHolder?>, PopupMenu.OnMenuItemClickListener {
    private val REMOVE: String = "Remove"
    private val dataItems = ArrayList<dataItem>()
    private var context: Context
    private var inflater: LayoutInflater
    private var position: Int = 0
    private var clickedON: Int = 0
    private var status: Int = 0
    private var playlist_name: String? = null
    private var playerService: PlayerService? = null

    constructor(context: Context, data: ArrayList<Int>) {
        this.context = context
        inflater = LayoutInflater.from(context)
        for (id: Int? in data) {
            val d: dataItem? = MusicLibrary.instance.getDataItemsForTracks()!![id]
            if (d != null) {
                dataItems.add(d)
            }
        }

        /*for (dataItem d: MusicLibrary.getInstance().getDataItemsForTracks()){
            if(data.contains(d.id))
                dataItems.add(d);
        }*/

        /*
        Collections.sort(dataItems, new Comparator<dataItem>() {
            @Override
            public int compare(dataItem o1, dataItem o2) {
                return o1.albumName.compareToIgnoreCase(o2.albumName);
            }
        });

        //add extra empty element
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));

        setHasStableIds(true);*/bindService()
    }

    //constructor for getMostPlayed and getRecentlyPlayed
    constructor(context: Context, data: ArrayList<dataItem>?, status: Int, playlist_name: String?) {
        this.playlist_name = playlist_name
        this.context = context
        inflater = LayoutInflater.from(context)
        dataItems.addAll((data)!!)
        //add extra empty element
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));
        setHasStableIds(true)
        bindService()
        this.status = status
    }

    fun shuffleAll() {
        //remove empty element from list of headers
        val temp: ArrayList<Int> = ArrayList()
        for (d: dataItem in dataItems) {
            if (d.id != 0) {
                temp.add(d.id)
            }
        }
        temp.shuffle()
        playerService!!.setTrackList(temp)
        playerService!!.playAtPosition(0)
    }

    fun bindService() {
        playerService = ApplicationClass.Companion.getService()
    }

    fun getList(): ArrayList<dataItem>? {
        return dataItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater.inflate(R.layout.fragment_library_item, parent, false)
        val viewParent: View = parent
        //int color = ColorHelper.getColor(R.color.colorwhite) ;
        //int color = ColorHelper.getBaseThemeTextColor();
        ((view.findViewById<View>(R.id.header)) as TextView).setTextColor(ColorHelper.getPrimaryTextColor())
        ((view.findViewById<View>(R.id.secondaryHeader)) as TextView).setTextColor(ColorHelper.getSecondaryTextColor())
        ((view.findViewById<View>(R.id.count)) as TextView).setTextColor(ColorHelper.getSecondaryTextColor())
        ((view.findViewById<View>(R.id.menuPopup)) as ImageView).setColorFilter(ColorHelper.getSecondaryTextColor())
        return MyViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("SecondaryLibraryAdapter", "onBindViewHolder: " + dataItems!!.get(position).title)
        if ((dataItems.get(position).title == "")) {
            holder.itemView.visibility = View.GONE
            return
        }
        holder.title.setPadding(20, 0, 0, 0)
        holder.secondary.setPadding(20, 0, 0, 0)
        val secondaryText = (dataItems[position].artist_name + " | " + dataItems[position].albumName)
        holder.title.text = dataItems[position].title
        holder.secondary.text = secondaryText
        holder.count.text = dataItems[position].durStr
    }

    override fun getItemCount(): Int {
        return dataItems.size
    }

    fun onClick(view: View, position: Int) {
        this.position = position
        if ((dataItems[position].title == "")) {
            return
        }
        clickedON = dataItems.get(position).id
        when (view.id) {
            R.id.libraryItem -> {
                if (ApplicationClass.Companion.isLocked()) {
                    Toast.makeText(context, "Music is Locked!", Toast.LENGTH_SHORT).show()
                    //Snackbar.make(viewParent, "Music is Locked!", Snackbar.LENGTH_SHORT).show();
                    return
                }
                Play()
            }
            R.id.menuPopup -> {
                val popup = PopupMenu(context, view)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_tracks_by_title, popup.menu)
                if (status == Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT) {
                    //popup.getMenu().removeItem(R.id.action_delete);
                    if (!(((playlist_name!!.replace(" ",
                            "_") == Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED) || (playlist_name!!.replace(
                            " ",
                            "_") == Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED) || (playlist_name!!.replace(
                            " ",
                            "_") == Constants.SYSTEM_PLAYLISTS.MOST_PLAYED)))
                    ) {
                        popup.menu.add(REMOVE)
                    }
                }
                popup.menu.removeItem(R.id.action_exclude_folder)
                popup.show()
                popup.setOnMenuItemClickListener(this)
            }
        }
    }

    fun updateItem(position: Int, vararg param: String) {
        dataItems[position].title = param[0]
        dataItems[position].artist_name = param[0]
        dataItems[position].albumName = param[0]
        notifyItemChanged(position)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                if (ApplicationClass.isLocked()) {
                    Toast.makeText(context,
                        context.getString(R.string.music_is_locked),
                        Toast.LENGTH_SHORT).show()
                    //Snackbar.make(viewParent, "Music is Locked!", Snackbar.LENGTH_SHORT).show();
                    return true
                }
                Play()
            }
            R.id.action_add_to_playlist -> {
                val ids: IntArray = intArrayOf(dataItems[position].id)
                UtilityFun.addToPlaylist(context, ids)
            }
            R.id.action_share -> {
                val files: ArrayList<Uri> = ArrayList()
                val fileToBeShared = File(MusicLibrary.instance.getTrackItemFromId(clickedON)!!.getFilePath())
                try {
                    files.add(FileProvider.getUriForFile(context,
                        context.applicationContext
                            .packageName + "com.aemerse.music.provider",
                        fileToBeShared))
                    UtilityFun.share(context, files, dataItems[position].title)
                } catch (e: IllegalArgumentException) {
                    try {
                        UtilityFun.shareFromPath(context, fileToBeShared.absolutePath)
                    } catch (ex: Exception) {
                        Toast.makeText(context,
                            context.getString(R.string.error_unable_to_share),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
            R.id.action_delete -> deleteDialog()
            R.id.action_play_next -> AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT)
            R.id.action_add_to_q -> AddToQ(Constants.ADD_TO_Q.AT_LAST)
            R.id.action_track_info -> setTrackInfoDialog()
            R.id.action_edit_track_info -> {
                val editItem: TrackItem? = MusicLibrary.instance.getTrackItemFromId(
                    dataItems.get(position).id)
                if (editItem == null) {
                    Toast.makeText(context,
                        context.getString(R.string.unknown_error),
                        Toast.LENGTH_SHORT).show()
                    //Snackbar.make(viewParent, "Error occurred!", Snackbar.LENGTH_SHORT).show();
                    return true
                }
                context.startActivity(Intent(context, ActivityTagEditor::class.java)
                    .putExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.SECONDARY_LIB)
                    .putExtra("file_path", editItem.getFilePath())
                    .putExtra("track_title", editItem.title)
                    .putExtra("position", position)
                    .putExtra("id", editItem.id))
            }
        }
        if ((item.title == REMOVE)) {
            PlaylistManager.getInstance(ApplicationClass.getContext())!!.RemoveSongFromPlaylistNew(playlist_name!!, dataItems!!.get(position).id)
            dataItems.removeAt(position)
            notifyItemRemoved(position)
        }
        return true
    }

    private fun setTrackInfoDialog() {
        //final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        //alert.setTitle(context.getString(R.string.track_info_title) );
        val linear: LinearLayout = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL
        val text = TextView(context)
        text.typeface = TypeFaceHelper.getTypeFace(context)
        text.text = UtilityFun.trackInfoBuild(dataItems[position].id).toString()
        text.setPadding(20, 20, 20, 10)
        text.textSize = 15f
        //text.setGravity(Gravity.CENTER);
        linear.addView(text)
        //alert.setView(linear);
        //alert.show()
        MaterialDialog(context)
            .title(R.string.track_info_title)
            .customView(view = linear, scrollable = true)
            .positiveButton(R.string.okay)
            .show()
    }

    private fun Play() {
        val temp: ArrayList<Int> = ArrayList()
        for (d: dataItem in dataItems) {
            if (d.id != 0) {
                temp.add(d.id)
            }
        }
        playerService!!.setTrackList(temp)
        playerService!!.playAtPosition(position)
    }

    private fun AddToQ(positionToAdd: Int) {
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        val toastString: String =
            (if (positionToAdd == Constants.ADD_TO_Q.AT_LAST) context.getString(R.string.added_to_q) else context.getString(
                R.string.playing_next))
        playerService!!.addToQ(clickedON, positionToAdd)
        //to update the to be next field in notification
        ApplicationClass.getService()!!.PostNotification()
        Toast.makeText(context, toastString + dataItems[position].title, Toast.LENGTH_SHORT).show()
        //Snackbar.make(viewParent, toastString+clikedON, Snackbar.LENGTH_SHORT).show();
    }

    private fun deleteDialog() {
        MaterialDialog(context)
            .title(R.string.are_u_sure)
            .positiveButton(R.string.yes){
                if (playerService!!.getCurrentTrack()?.title.equals(dataItems[position].title)) {
                    Toast.makeText(context, context.getString(R.string.song_is_playing), Toast.LENGTH_SHORT).show()
                    // Snackbar.make(viewParent, "Cannot delete currently playing song", Snackbar.LENGTH_SHORT).show();
                    return@positiveButton
                }
                val file: File
                try {
                    file = File(dataItems[position]?.let { it1 -> MusicLibrary.instance.getTrackItemFromId(it1.id)?.getFilePath() })
                } catch (e: Exception) {
                    return@positiveButton
                }
                //delete the file first
                val files: ArrayList<File> = ArrayList()
                files.add(file)
                val ids: ArrayList<Int> = ArrayList()
                dataItems[position].id.let { it1 -> ids.add(it1) }
                when {
                    UtilityFun.delete(context, files, ids) -> {
                        Toast.makeText(context, context.getString(R.string.deleted) + dataItems.get(position).title, Toast.LENGTH_SHORT).show()
                        dataItems.remove(dataItems[position])
                        notifyItemRemoved(position)
                        notifyDataSetChanged()
                    }
                    else -> {
                        Toast.makeText(context, context.getString(R.string.unable_to_del), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .negativeButton(R.string.no)
            .show()
    }

    inner class MyViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView = itemView.findViewById(R.id.header)
        var secondary: TextView = itemView.findViewById(R.id.secondaryHeader)
        var count: TextView = itemView.findViewById(R.id.count)
        var popUp: ImageButton

        override fun onClick(view: View) {
            this@SecondaryLibraryAdapter.onClick(view, layoutPosition)
        }

        init {
            //title.setTypeface(TypeFaceHelper.getTypeFace());
            //secondary.setTypeface(TypeFaceHelper.getTypeFace());
            //count.setTypeface(TypeFaceHelper.getTypeFace());
            itemView.findViewById<View>(R.id.album_art_wrapper).visibility = View.GONE
            popUp = itemView.findViewById(R.id.menuPopup)
            itemView.setOnClickListener(this)
            itemView.findViewById<View>(R.id.menuPopup).setOnClickListener(this)
            itemView.findViewById<View>(R.id.imageVIewForStubAlbumArt).visibility = View.GONE
        }
    }
}