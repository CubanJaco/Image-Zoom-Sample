package cu.jaco.imagezoomsample

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import kotlin.math.abs

class ImageTouchHandler(context: Context, private val listener: ImageTouchListener? = null) :
    ImageMatrixTouchHandler(context) {

    private var touchedView: View? = null
    private var imageGestureDetector = ImageGestureDetector()
    private var gestureDetector: GestureDetector = GestureDetector(context, imageGestureDetector)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        val aux = super.onTouch(view, event)

        touchedView = view
        gestureDetector.onTouchEvent(event)

        val imageView = try {
            view as ImageView
        } catch (e: ClassCastException) {
            throw IllegalStateException("View must be an instance of ImageView", e)
        }

        // Get the matrix
        val matrix = imageView.imageMatrix

        val values = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        matrix.getValues(values)

        val tag = imageView.tag as Float
        val curr = values[Matrix.MSCALE_X]
        listener?.onImageZoomed(abs(tag - curr) >= 0.001)

        return aux
    }

    private inner class ImageGestureDetector: GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            touchedView?.performClick()
            return super.onSingleTapConfirmed(e)
        }

    }

    interface ImageTouchListener {

        fun onImageZoomed(zoomed: Boolean)

    }
}