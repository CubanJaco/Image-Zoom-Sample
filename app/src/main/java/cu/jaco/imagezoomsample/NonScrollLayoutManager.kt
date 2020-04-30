package cu.jaco.imagezoomsample

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NonScrollLayoutManager(context: Context, @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean):
    LinearLayoutManager(context, orientation, reverseLayout) {

    var isScrollEnabled = true

    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled && super.canScrollHorizontally()
    }
}