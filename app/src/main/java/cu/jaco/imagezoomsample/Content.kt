package cu.jaco.imagezoomsample

import android.content.Context
import java.io.Serializable
import java.util.*

/**
 * Helper class for providing content for user interfaces
 */
class Content(context: Context) {

    companion object {

        const val CATEGORIES_FOLDER = "categories"

    }

    /**
     * An array of items.
     */
    val ITEMS: MutableList<ImageItem> = ArrayList()

    init {

        context.getItems()

    }

    private fun Context.getItems() {

        for (file in assets.list("$CATEGORIES_FOLDER/Test") ?: arrayOf()) {

            ITEMS.add(
                ImageItem(
                    folder = "Test",
                    image = file,
                    type = ImageItem.ASSET
                )
            )

        }

    }

    data class ImageItem(
        val folder: String = "",
        val image: String = "",
        val resource: Int = R.drawable.place_holder,
        val type: Int = ASSET
    ) : Serializable {

        companion object {
            const val ASSET = 1
            const val FILE = 2
        }

        fun getPath(): String {

            return if (folder.isNotEmpty() && type == ASSET) {
                "${CATEGORIES_FOLDER}/${folder}/${image}"
            } else if (folder.isNotEmpty() && type == FILE) {
                "${folder}/${image}"
            } else {
                image
            }

        }
    }
}
