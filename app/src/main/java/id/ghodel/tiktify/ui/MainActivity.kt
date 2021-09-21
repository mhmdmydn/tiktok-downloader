package id.ghodel.tiktify.ui

import android.Manifest
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import id.ghodel.tiktify.api.RetrofitClient
import id.ghodel.tiktify.data.VideoMeta
import id.ghodel.tiktify.databinding.ActivityMainBinding
import id.ghodel.tiktify.utils.AsyncResponse
import id.ghodel.tiktify.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), AsyncResponse {

    private lateinit var binding: ActivityMainBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var progressDownload: ProgressDialog
    private val api by lazy {
        RetrofitClient.getData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val url = intent.getStringExtra("URL")
        Log.d(MainActivity::class.java.simpleName, url!!)

        // Setting timeout globally for the download network requests:
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(this, config)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Search Video")
        progressDialog.setMessage("Please wait getting from server...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.isIndeterminate = true
        progressDialog.show()

        api.getVideoMeta(url).enqueue(object :
            Callback<VideoMeta> {
            override fun onResponse(
                call: Call<VideoMeta>,
                response: Response<VideoMeta>
            ) {
                when {
                    response.message() == "OK" -> {
                        Log.d("Results", response.message())
                        progressDialog.dismiss()
                        //getting all results
                        val data = response.body()!!.results

                        Log.d("DATA" , data.toString())
                        //getting status response from server
                        val responseServer = response.body()!!.status

                        //check if status false
                        if(responseServer){
                            //looping data results video
                            data.forEach { video ->

                                Glide.with(applicationContext).load(video.thumbnail).into(binding.imgThumbnail)
                                Glide.with(applicationContext).load(video.author.avatar).into(binding.imgProfile)
                                binding.tvTitle.text = video.author.name
                                binding.tvSubtitle.text = if(video.text.isNotEmpty()) video.text else "No Description"
                                binding.tvWatch.text = video.watch.toString()
                                binding.tvLikes.text = video.likes.toString()
                                binding.tvComment.text = video.comment.toString()
                                binding.tvMusic.text = video.music.musicName

                                val videoWithWm = video.withWatermark
                                val videoWithoutWm = video.withoutWatermark

                                //button download with wm
                                binding.btnWithWm.setOnClickListener {
                                  Dexter.withContext(this@MainActivity).withPermissions(
                                        Manifest.permission.INTERNET,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ).withListener(object : MultiplePermissionsListener{
                                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                                            report.let {
                                                if(report?.areAllPermissionsGranted() == true){
                                                    //startDownload(videoWithWm)
                                                    downloadFromUrl(videoWithWm, video.text)
                                                   // val downloadVideoWithWM = DownloadVideo(this@MainActivity, this@MainActivity)
                                                   // downloadVideoWithWM.execute(videoWithWm)

                                                }
                                            }

                                        }

                                        override fun onPermissionRationaleShouldBeShown(
                                            permissions: MutableList<PermissionRequest>?,
                                            token: PermissionToken?
                                        ) {
                                            token?.continuePermissionRequest()
                                        }

                                    }).withErrorListener { error ->
                                        Log.d("Permissions", error.name)
                                    }.check()
                                }

                                //button download without wm
                                binding.btnWithoutWm.setOnClickListener {
                                    Dexter.withContext(this@MainActivity).withPermissions(
                                        Manifest.permission.INTERNET,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ).withListener(object : MultiplePermissionsListener{
                                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                                            report.let {
                                                if(report?.areAllPermissionsGranted() == true){
                                                    //val downloadVideoWithoutWM = DownloadVideo(this@MainActivity, this@MainActivity)
                                                    //downloadVideoWithoutWM.execute(videoWithoutWm)
                                                    //startDownload(videoWithoutWm)
                                                    downloadFromUrl(videoWithWm, video.text)
                                                }
                                            }

                                        }

                                        override fun onPermissionRationaleShouldBeShown(
                                            permissions: MutableList<PermissionRequest>?,
                                            token: PermissionToken?
                                        ) {
                                            token?.continuePermissionRequest()
                                        }

                                    }).withErrorListener { error ->
                                        Log.d("Permissions", error.name)
                                    }.check()

                                }
                            }
                        } else {
                            progressDialog.dismiss()
                        }


                    }
                }

            }

            override fun onFailure(call: Call<VideoMeta>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                Log.d("Error", t.message.toString())
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onSuccess(strPath: String) {
        Log.d(this::class.java.simpleName, strPath)
        Toast.makeText(applicationContext, strPath, Toast.LENGTH_SHORT).show()
    }

    private fun startDownload(url: String){

        progressDownload = ProgressDialog(this)
        progressDownload.setTitle("Download Video")
        progressDownload.setMessage("Please wait...")
        progressDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDownload.setCancelable(false)
        progressDownload.isIndeterminate = true
        progressDownload.show()

        val dirPath = Environment.DIRECTORY_DOWNLOADS
        val fileName = "TT_VIDEO_${System.currentTimeMillis()}.mp4"
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(this, config)

        PRDownloader.download(url, dirPath, fileName)
            .build()
            .setOnStartOrResumeListener {
                progressDownload.setMessage("Download video started...")
            }
            .setOnPauseListener {

            }
            .setOnCancelListener{

            }
            .setOnProgressListener { progress->
                val progressPercent: Long = progress.currentBytes * 100 / progress.totalBytes
                progressDownload.max = 100
                progressDownload.progress = progressPercent.toInt()
                progressDownload.setMessage("Processing..." + Utils.getProgressDisplayLine(progress.currentBytes, progress.totalBytes))
                progressDownload.isIndeterminate = false

            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    progressDownload.dismiss()
                    showMsg("Download complete. $dirPath$fileName")
                }

                override fun onError(error: com.downloader.Error?) {
                    progressDownload.dismiss()
                    showMsg("Download failed : $error")
                }
            })
    }

    private fun downloadFromUrl(url: String, title: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle("Download Video")
            request.setDescription(title)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                System.currentTimeMillis().toString()
            )
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (exception: Exception) {
            Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showMsg(text: String){
        progressDialog.dismiss()
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

}