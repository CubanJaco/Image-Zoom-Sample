package cu.jaco.imagezoomsample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), ImageTouchHandler.ImageTouchListener,
    ImageFullAdapter.OnImageInteractionListener, View.OnClickListener {


    private lateinit var lManager: NonScrollLayoutManager
    private var lastWallpaperClick: Long = 0
    private lateinit var adapter: ImageFullAdapter
    private lateinit var snapHelper: PagerSnapHelper
    private var imageDisposable: Disposable? = null

    companion object {

        private const val REQUEST_PERMISSION = 7652
        private const val IMAGE_NAME = "img_wallpaper.jpg"

        const val ARG_IMAGE = "ARG_IMAGE"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val img = intent.getSerializableExtra(ARG_IMAGE) as Content.ImageItem?
        val imgs = Content(this).ITEMS

        adapter = ImageFullAdapter(
            items = imgs,
            touchHandler = ImageTouchHandler(this, this),
            itemListener = this
        )
        recycler_view.adapter = adapter

        lManager = NonScrollLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.layoutManager = lManager

        //creare pager snap
        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recycler_view)

        //comenzar el adapter en la imagen seleccionada
        val pos = imgs.indexOf(img)
        recycler_view.scrollToPosition(if (pos == -1) 0 else pos)

        //actualizar el tag cada vez que se entra en una nueva imagen
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    //obtener el layout manager
                    val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
                    //obtener el item visible
                    val position = layoutManager.findFirstVisibleItemPosition()
                    //obtener el holder
                    val holder = recyclerView.findViewHolderForAdapterPosition(position)
                    //establecer el tag
                    (holder as ImageFullAdapter.WallpaperHolder).setTag()

                }
            }
        })

        prepareMenu()
        showMenu()

    }

    override fun onDestroy() {
        super.onDestroy()

        if (imageDisposable?.isDisposed == true)
            imageDisposable?.dispose()

    }

    private fun prepareMenu() {

        action_set_wallpaper.setOnClickListener(this)
        action_share.setOnClickListener(this)
        action_save.setOnClickListener(this)

    }

    override fun onImageZoomed(zoomed: Boolean) {

        lManager.isScrollEnabled = !zoomed

    }

    override fun onImageClick() {
        lastWallpaperClick = System.nanoTime()

        if (mIsMenuVisible())
            hideMenu()
        else
            showMenu()

    }

    private fun mIsMenuVisible(): Boolean {
        return wallpaper_menu.visibility == View.VISIBLE
    }

    private fun hideMenu() {
        val out = TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            1f
        )
        out.duration = 300
        wallpaper_menu.startAnimation(out)
        wallpaper_menu.visibility = View.GONE
    }

    private fun showMenu() {
        val animateIn = TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            1f,
            Animation.RELATIVE_TO_SELF,
            0f
        )
        animateIn.duration = 300
        wallpaper_menu.startAnimation(animateIn)
        wallpaper_menu.visibility = View.VISIBLE
        hideDelayed()
    }

    private fun hideDelayed() {
        Handler().postDelayed({
            val time = System.nanoTime() - lastWallpaperClick
            val seconds = time.toDouble() / (1000.0 * 1000000.0)
            if (mIsMenuVisible() && seconds >= 3.5) hideMenu()
        }, 4000)
    }

    override fun onClick(v: View) {
        val position = snapHelper.findTargetSnapPosition(lManager, 0, 0)

        when (v.id) {
            R.id.action_share -> {
                shareImage(position)
            }
            R.id.action_save -> {
                save(position)
            }
            R.id.action_set_wallpaper -> {
                setAsImage(position)
            }
        }

        if (mIsMenuVisible())
            hideMenu()

    }

    private fun requestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )

        }

    }

    private fun hasPermissions(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return false

        }

        return true

    }

    private fun shareImage(position: Int) {

        //share image

    }

    private fun setAsImage(position: Int) {

        //set image as

    }

    private fun save(position: Int, requestPermission: Boolean = true): Boolean {

        if (position == -1)
            return false

        val hasPermission = hasPermissions()

        if (!hasPermission && requestPermission) {
            requestPermissions()
            return false
        } else if (!hasPermission) {
            return false
        }

        //save your image

        return true

    }

    private fun getImageFile(fileName: String = IMAGE_NAME): File {
        val mediaStorageDir = filesDir
        return File(mediaStorageDir, fileName)
    }

}