package id.ghodel.tiktify.api

import id.ghodel.tiktify.data.VideoMeta
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET ("api/v1/tt")
    fun getVideoMeta(@Query("url") url: String) : Call<VideoMeta>
}