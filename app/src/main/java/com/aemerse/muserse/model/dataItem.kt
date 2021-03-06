package com.aemerse.muserse.model


class dataItem {
    constructor(
        id: Int,
        title: String?,
        artist_id: Int,
        artist_name: String?,
        album_id: Int,
        albumName: String?,
        year: String?,
        file_path: String?,
        duration: String,
        trackNumber: Int
    ) {
        if (title != null) {
            this.title = title
        }
        this.id = id
        this.album_id = album_id
        if (albumName != null) {
            this.albumName = albumName
        }
        this.artist_id = artist_id
        if (artist_name != null) {
            this.artist_name = artist_name
        }
        this.file_path = file_path
        if (year != null) this.year = year

        //Log.v("Year", title + " : " + year);
        this.duration = duration
        if (duration != "") {
            durStr = getDurStr()
        }
        this.trackNumber = trackNumber
    }

    constructor(id: Int, title: String?, numberOfTracks: Int, numberOfAlbums: Int) {
        artist_id = id
        if (title != null) {
            this.title = title
            artist_name = title
        }
        this.numberOfTracks = numberOfTracks
        this.numberOfAlbums = numberOfAlbums
    }

    constructor(
        id: Int,
        title: String?,
        artist_name: String,
        numberOfTracks: Int,
        year: String?,
        artist_id: Int
    ) {
        this.artist_name = artist_name
        this.artist_id = artist_id
        if (title != null) {
            this.title = title
            albumName = title
        }
        album_id = id

        // Log.v("Year", title + " : " + year);
        if (year != null) this.year = year
        this.numberOfTracks = numberOfTracks
    }

    constructor(genre_id: Int, genre_name: String?, numberOfTracks: Int) {
        id = genre_id
        if (genre_name != null) {
            title = genre_name
        }
        this.numberOfTracks = numberOfTracks
    }

    var id = 0
    var title = ""
    var artist_id = 0
    var artist_name = ""
    var album_id = 0
    var albumName = ""
    var year = "zzzz"
    var numberOfTracks = 0
    var numberOfAlbums = 0
    var file_path: String? = null
    var duration: String? = null
    var durStr: String? = null
    var trackNumber = 0

    @JvmName("getDurStr1")
    private fun getDurStr(): String {
        var minutes = 0
        var seconds = 0
        try {
            minutes = duration!!.toInt() / 1000 / 60
            seconds = duration!!.toInt() / 1000 % 60
        } catch (ignored: NumberFormatException) {
        }
        val durFormatted = String.format("%02d", seconds)
        return "$minutes:$durFormatted"
    }
}