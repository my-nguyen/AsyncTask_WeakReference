package com.florian_walther.asynctask_weakreference

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.florian_walther.asynctask_weakreference.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val task = WeakAsyncTask(this)
            task.execute(10)
        }
    }

    // since this is an inner class, it holds a (strong) reference to the outer Activity. if the
    // Activity gets destroyed while the AsyncTask is still running (still doInBackground for
    // example), then the reference can't be garbage collected, resulting in a memory leak.
    inner class StrongAsyncTask(): AsyncTask<Int, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Int?): String {
            val max = params[0]!!
            for (i in 1..max) {
                publishProgress((i*100) / max)
                Thread.sleep(1000)
            }
            return "Finished!"
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            binding.progressBar.progress = values[0]!!
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
            binding.progressBar.progress = 0
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    // with a WeakReference, garbage collector can still destroy this reference, so there won't be
    // memory leak
    class WeakAsyncTask(activity: MainActivity): AsyncTask<Int, Int, String>() {
        private val weakReference = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()

            val activity = weakReference.get()
            if (activity != null && !activity.isFinishing) {
                activity.binding.progressBar.visibility = View.VISIBLE
            }
        }

        override fun doInBackground(vararg params: Int?): String {
            val max = params[0]!!
            for (i in 1..max) {
                publishProgress((i*100) / max)
                Thread.sleep(1000)
            }
            return "Finished!"
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)

            val activity = weakReference.get()
            if (activity != null && !activity.isFinishing) {
                activity.binding.progressBar.progress = values[0]!!
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val activity = weakReference.get()
            if (activity != null && !activity.isFinishing) {
                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show()
                activity.binding.progressBar.progress = 0
                activity.binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }
}