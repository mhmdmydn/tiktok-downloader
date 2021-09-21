package id.ghodel.tiktify.data

import androidx.annotation.Keep

@Keep
data class VideoMeta(
    val developer: String,
    val results: List<Result>,
    val status: Boolean,
    val url: String
)
@Keep
data class Result(
    val author: Author,
    val comment: Int,
    val hashtag: List<Hashtag>,
    val id: String,
    val likes: Int,
    val mention: List<Any>,
    val music: Music,
    val share: Int,
    val text: String,
    val thumbnail: String,
    val watch: Int,
    val withWatermark: String,
    val withoutWatermark: String
)
@Keep
data class Author(
    val avatar: String,
    val digg: Int,
    val fans: Int,
    val following: Int,
    val heart: Int,
    val id: String,
    val name: String,
    val nickName: String,
    val `private`: Boolean,
    val secUid: String,
    val signature: String,
    val verified: Boolean,
    val video: Int
)
@Keep
data class Hashtag(
    val cover: String,
    val id: String,
    val name: String,
    val title: String
)
@Keep
data class Music(
    val coverLarge: String,
    val coverMedium: String,
    val coverThumb: String,
    val duration: Int,
    val musicAuthor: String,
    val musicId: String,
    val musicName: String,
    val musicOriginal: Boolean
)