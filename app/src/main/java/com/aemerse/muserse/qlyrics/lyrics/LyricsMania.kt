/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.aemerse.muserse.qlyrics.lyrics

import com.aemerse.muserse.qlyrics.lyricsAndArtistInfo.Net
import com.aemerse.muserse.qlyrics.annotations.Reflection
import com.aemerse.muserse.qlyrics.lyrics.Lyrics.Companion.ERROR
import com.aemerse.muserse.qlyrics.lyrics.Lyrics.Companion.NEGATIVE_RESULT
import com.aemerse.muserse.qlyrics.lyrics.Lyrics.Companion.POSITIVE_RESULT
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Whitelist
import java.io.IOException
import java.text.Normalizer
import java.util.*

@Reflection
object LyricsMania {
    @Reflection
    val domain: String = "www.lyricsmania.com"
    private val baseURL: String = "http://www.lyricsmania.com/%s_lyrics_%s.html"
    @Reflection
    fun fromMetaData(artist: String, song: String): Lyrics {
        var htmlArtist: String =
            Normalizer.normalize(artist.replace("[\\s-]".toRegex(), "_"), Normalizer.Form.NFD)
                .replace("[^\\p{ASCII}]".toRegex(), "").replace("[^A-Za-z0-9_]".toRegex(), "")
        val htmlSong: String =
            Normalizer.normalize(song.replace("[\\s-]".toRegex(), "_"), Normalizer.Form.NFD)
                .replace("[^\\p{ASCII}]".toRegex(), "").replace("[^A-Za-z0-9_]".toRegex(), "")
        if (artist.startsWith("The ")) htmlArtist = htmlArtist.substring(4) + "_the"
        val urlString: String = String.format(
            baseURL,
            htmlSong.lowercase(Locale.getDefault()),
            htmlArtist.lowercase(Locale.getDefault()))
        return fromURL(urlString, artist, song)
    }

    @Reflection
    fun fromURL(url: String?, artist: String?, title: String?): Lyrics {
        var artist: String? = artist
        var title: String? = title
        var text: String
        try {
            val document: Document = Jsoup.connect(url).userAgent(Net.USER_AGENT).get()
            val lyricsBody: Element = document.getElementsByClass("lyrics-body").get(0)
            // lyricsBody.select("div").last().remove();
            text = Jsoup.clean(lyricsBody.html(), "", Whitelist.basic().addTags("div"))
            text = text.substring(text.indexOf("</strong>") + 10, text.lastIndexOf("</div>"))
            val keywords: Array<String> =
                document.getElementsByTag("meta").attr("name", "keywords").get(0).attr("content")
                    .split(",".toRegex()).toTypedArray()
            if (artist == null) artist = document.getElementsByClass("lyrics-nav-menu").get(0)
                .getElementsByTag("a").get(0).text()
            if (title == null) title = keywords.get(0)
        } catch (e: HttpStatusException) {
            return Lyrics(Lyrics.NO_RESULT)
        } catch (e: IndexOutOfBoundsException) {
            return Lyrics(Lyrics.NO_RESULT)
        } catch (e: IOException) {
            return Lyrics(ERROR)
        }
        if (text.startsWith("Instrumental")) return Lyrics(NEGATIVE_RESULT)
        val lyrics = Lyrics(POSITIVE_RESULT)
        lyrics.setArtist(artist)
        lyrics.setTitle(title)
        lyrics.setURL(url)
        lyrics.setSource(domain)
        lyrics.setText(text.trim { it <= ' ' })
        return lyrics
    }
}