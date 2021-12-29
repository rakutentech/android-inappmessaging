package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import timber.log.Timber

internal class InAppMessagingTooltipView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), InAppMessageView {

    init {
        id = R.id.in_app_message_tooltip_view
    }

    private var imageUrl: String? = null
    private var bgColor = "#FFFFFF" // default white
    private var type: PositionType = PositionType.BOTTOM_CENTER

    @VisibleForTesting
    internal var picasso: Picasso? = null

    override fun populateViewData(message: Message) {
        this.imageUrl = message.getMessagePayload().resource.imageUrl
        message.getTooltipConfig()?.color?.let {
            bgColor = it
        }
        message.getTooltipConfig()?.position?.let { position ->
            PositionType.getById(position)?.let {
                type = it
            }
        }
        val listener = InAppMessageViewListener(message)
        setOnClickListener(listener)
        findViewById<ImageButton>(R.id.message_close_button)?.setOnClickListener(listener)
        bindImage()
    }

    /**
     * This method binds image to view.
     */
    @Suppress("ClickableViewAccessibility", "TooGenericExceptionCaught", "LongMethod")
    private fun bindImage() { // Display image.
        if (!this.imageUrl.isNullOrEmpty()) {

            // load the image then display the view
            this.visibility = GONE
            findViewById<ImageView>(R.id.message_tooltip_image_view)?.let {
                try {
                    val callback = object : Callback {
                        override fun onSuccess() {
                            it.visibility = VISIBLE
                            this@InAppMessagingTooltipView.visibility = VISIBLE
                        }

                        override fun onError(e: Exception?) {
                            Timber.tag(TAG).d(e?.cause, "Downloading image failed $imageUrl")
                        }
                    }

                    it.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            if (it.width > 0) {
                                setBackground(it.width, it.height)
                                setTip(it.width, it.height)
                                it.viewTreeObserver.removeOnPreDrawListener(this)
                            }
                            return true
                        }
                    })
                    (picasso ?: Picasso.get()).load(this.imageUrl)
                        .priority(Picasso.Priority.HIGH)
                        .resize(MAX_SIZE, MAX_SIZE)
                        .onlyScaleDown()
                        .centerInside()
                        .into(it, callback)
                } catch (ex: Exception) {
                    Timber.tag(TAG).d(ex, "Downloading image failed $imageUrl")
                }
            }
        }
    }

    private fun setBackground(width: Int, height: Int) {

        val paint = Paint()
        paint.color = Color.parseColor(bgColor)
        val bg = Bitmap.createBitmap(width + PADDING, height + PADDING, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bg)

        val path = Path()
        val corners = floatArrayOf(RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS)
        path.addRoundRect(
            RectF(0f, 0f, (width + PADDING).toFloat(), (height + PADDING).toFloat()),
            corners,
            Path.Direction.CW
        )
        canvas.drawPath(path, paint)
        val imageView = findViewById<ImageView>(R.id.message_background)
        imageView.setImageBitmap(bg)
    }

    private fun setTip(width: Int, height: Int) {
        var adjustedWidth = width
        var adjustedHeight = height
        val ptArray = when (type) {
            PositionType.BOTTOM_CENTER -> {
                adjustedHeight += PADDING
                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(0, 0, 0, 2 * TRI_SIZE)
                findViewById<ImageView>(R.id.message_background)?.setPadding(0, 0, 0, 2 * TRI_SIZE)
                val posY = height + TRI_SIZE / 2
                arrayOf(
                    Point(width / 2 - TRI_SIZE, posY),
                    Point(width / 2, posY + TRI_SIZE),
                    Point(width / 2 + TRI_SIZE, posY)
                )
            }
            PositionType.TOP_CENTER -> {
                adjustedHeight += PADDING
                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(0, 2 * TRI_SIZE, 0, 0)
                findViewById<ImageView>(R.id.message_background)?.setPadding(0, 2 * TRI_SIZE, 0, 0)
                arrayOf(
                    Point(width / 2, TRI_SIZE / 2),
                    Point(width / 2 - TRI_SIZE, TRI_SIZE + TRI_SIZE / 2),
                    Point(width / 2 + TRI_SIZE, TRI_SIZE + TRI_SIZE / 2)
                )
            }
            PositionType.LEFT -> {
                adjustedWidth += PADDING
                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(2 * TRI_SIZE, 0, 0, 0)
                findViewById<ImageView>(R.id.message_background)?.setPadding(2 * TRI_SIZE, 0, 0, 0)
                arrayOf(
                    Point(TRI_SIZE + TRI_SIZE, height / 2 - TRI_SIZE),
                    Point(TRI_SIZE, height / 2),
                    Point(TRI_SIZE + TRI_SIZE, height / 2 + TRI_SIZE)
                )
            }
            PositionType.RIGHT -> {
                adjustedWidth += PADDING
                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(0, 0, 2 * TRI_SIZE, 0)
                findViewById<ImageView>(R.id.message_background)?.setPadding(0, 0, 2 * TRI_SIZE, 0)
                arrayOf(
                    Point(width, height / 2 - TRI_SIZE),
                    Point(width + TRI_SIZE, height / 2),
                    Point(width, height / 2 + TRI_SIZE)
                )
            }
            else -> arrayOf()
        }

        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(ptArray[0].x.toFloat(), ptArray[0].y.toFloat())
        path.lineTo(ptArray[1].x.toFloat(), ptArray[1].y.toFloat())
        path.lineTo(ptArray[2].x.toFloat(), ptArray[2].y.toFloat())
        path.close()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.strokeWidth = 2f
        paint.color = Color.parseColor(bgColor)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        val bg = Bitmap.createBitmap(adjustedWidth, adjustedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bg)
        canvas.drawPath(path, paint)

        val imageView = findViewById<ImageView>(R.id.message_tip)
//        imageView.setBackgroundColor(Color.parseColor("#A5000000"))
        imageView.setImageBitmap(bg)
    }

    companion object {
        private const val TAG = "IAM_ToolTipView"
        private const val PADDING = 40
        private const val TRI_SIZE = 20
        private const val RADIUS = 20f
        private const val MAX_SIZE = 600
    }
}
