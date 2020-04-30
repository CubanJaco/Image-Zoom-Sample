package cu.jaco.imagezoomsample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.image_full_item.view.*

class ImageFullAdapter(
    val items: MutableList<Content.ImageItem>,
    private var touchHandler: ImageTouchHandler? = null,
    private var itemListener: OnImageInteractionListener? = null,
    private val placeholder: Int = R.drawable.place_holder
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var placeHolder: Bitmap
    lateinit var mRecyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_full_item, parent, false)
        return WallpaperHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (::placeHolder.isInitialized)
            placeHolder =
                BitmapFactory.decodeResource(holder.itemView.resources, placeholder)

        (holder as WallpaperHolder).bind(items[position])

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as WallpaperHolder).recycle()
        super.onViewRecycled(holder)
    }

    inner class WallpaperHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var imageDisposable: Disposable? = null

        fun bind(item: Content.ImageItem) {

            loadImageAsync(item)
            itemView.setOnClickListener {
                itemListener?.onImageClick()
            }

        }

        fun recycle() {

            itemView.image_full.imageMatrix = Matrix() //restablecer la matriz sin variaciones
            itemView.image_full.scaleType =
                ImageView.ScaleType.FIT_CENTER //restablecer la posicion de la imagen

            if (imageDisposable?.isDisposed == false)
                imageDisposable?.dispose()

        }

        private fun loadImageAsync(item: Content.ImageItem) {

            itemView.image_full.setImageResource(placeholder)

            val options = BitmapFactory.Options()

            imageDisposable = Observable.create<Bitmap> {
                it.onNext(
                    BitmapFactory.decodeStream(
                        itemView.context.assets?.open(item.getPath()),
                        null, options
                    ) ?: BitmapFactory.decodeResource(
                        itemView.context.resources,
                        R.drawable.place_holder,
                        options
                    )
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Bitmap?>() {

                    override fun onComplete() {
                    }

                    override fun onNext(bitmap: Bitmap) {

                        if (touchHandler == null)
                            touchHandler = ImageTouchHandler(itemView.context)

                        itemView.image_full.setImageBitmap(bitmap)
                        itemView.image_full.setOnTouchListener(touchHandler)

                        setTag()
                    }

                    override fun onError(e: Throwable) {
                        itemView.image_full.setImageResource(placeholder)
                    }

                })
        }

        fun setTag() {

            val values = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            itemView.image_full.imageMatrix.getValues(values)
            itemView.image_full.tag = values[Matrix.MSCALE_X]

        }

    }

    interface OnImageInteractionListener {

        fun onImageClick()

    }

}