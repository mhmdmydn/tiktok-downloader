package id.ghodel.tiktify.utils

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import id.ghodel.tiktify.R
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("StaticFieldLeak")
class DownloadVideo(private val context: Context, private val asyncResponse: AsyncResponse) : AsyncTask<String?, Int?, String?>() {

    private lateinit var progressDialog: ProgressDialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPreExecute() {
        super.onPreExecute()

        if(isInternetAvailable(context)){
            progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Downloading Video")
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressDialog.isIndeterminate = true
            progressDialog.show()
        }

    }

    @Suppress("UNREACHABLE_CODE")
    override fun doInBackground(vararg params: String?): String? {

        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try{
            val url = URL(params[0])
            Log.d("URL", url.toString())

            connection = (url.openConnection() as HttpURLConnection?)!!
            connection.requestMethod = "GET"
            connection.doOutput = true
            connection.addRequestProperty(
                "User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.94 Chrome/37.0.2062.94 Safari/537.36"
            ) // add this line to your code
            connection.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file

            if(connection.responseCode != HttpURLConnection.HTTP_OK){
                return "Server returned HTTP " + connection.responseCode
                .toString() + " " + connection.responseMessage
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            val fileLength: Int = connection.contentLength

            val folder = File(Environment.DIRECTORY_DOWNLOADS + "/tiktok")

            if(!folder.exists()){
                folder.mkdir()
            }

            val outputVideo = File(folder, "tiktok_vid_${System.currentTimeMillis()}" + ".mp4")

            // download the file
            input = BufferedInputStream(connection.inputStream, 8192)

            Log.d("is Success",connection.inputStream.available().toString())
            Log.d("is Error", connection.errorStream.toString())
            output = FileOutputStream(outputVideo)


            val data = ByteArray(1024)
            var total: Long = 0
            val count: Int = input.read(data)

            while (count  != -1) {
                // allow canceling with back button
                    if (isCancelled) {
                        input.close()
                        return null
                    }
                total += count.toLong()
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((total * 100 / fileLength).toInt())
                output.write(data, 0, count)
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            return outputVideo.toString()

        } catch (e: Exception) {
            return e.message
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        progressDialog.isIndeterminate = false;
        progressDialog.max = 100;
        progressDialog.progress = values[0]!!;
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        progressDialog.dismiss()

        if(result != null){
            asyncResponse.onSuccess(result)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}