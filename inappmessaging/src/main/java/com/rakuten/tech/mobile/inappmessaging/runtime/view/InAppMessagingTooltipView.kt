package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.drawable.ScaleDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.VisibleForTesting
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ResourceUtils
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@SuppressWarnings("LargeClass", "LongMethod")
internal class InAppMessagingTooltipView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(context, attrs), InAppMessageView {

    init {
        id = R.id.in_app_message_tooltip_view
    }

    private var imageUrl: String? = null
    private var bgColor = "#FFFFFF" // default white
    internal var type: PositionType = PositionType.BOTTOM_CENTER
    private var viewId: String? = null
    private var listener: InAppMessageViewListener? = null
    internal var isTest = false
    internal var mainHandler = Handler(Looper.getMainLooper())

    @VisibleForTesting
    internal var picasso: Picasso? = null

    override fun populateViewData(message: Message) {
        // set tag
        tag = message.getCampaignId()
        this.imageUrl = message.getMessagePayload().resource.imageUrl
        this.bgColor = message.getMessagePayload().backgroundColor
        message.getTooltipConfig()?.let { tooltip ->
            val position = PositionType.getById(tooltip.position)
            if (position != null) {
                type = position
            }
            viewId = tooltip.id
        }
        listener = InAppMessageViewListener(message)
        bindImage()
    }

    /**
     * This method binds image to view.
     */
    @Suppress("ClickableViewAccessibility", "TooGenericExceptionCaught", "LongMethod")
    private fun bindImage() { // Display image.
        if (!this.imageUrl.isNullOrEmpty()) {
            // load the image then display the view
            this.visibility = INVISIBLE
            findViewById<ImageView>(R.id.message_tooltip_image_view).let {
                try {
                    val callback = object : Callback {
                        override fun onSuccess() {
                            it.visibility = INVISIBLE
                            this@InAppMessagingTooltipView.visibility = INVISIBLE
                        }

                        override fun onError(e: Exception?) {
                            InAppLogger(TAG).debug(e?.cause, "Downloading image failed $imageUrl")
                        }
                    }

                    it.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (it.width > 0 || isTest) {
                                it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                setBackground(it.width, it.height)
                                setTip()
                                showView()
                                // to avoid flicker
                                mainHandler.postDelayed({
                                    it.visibility = VISIBLE
                                    this@InAppMessagingTooltipView.visibility = VISIBLE
                                }, DELAY)
                            }
                        }
                    })
                    (picasso ?: Picasso.get()).load(this.imageUrl)
                        .priority(Picasso.Priority.HIGH)
                        .resize(MAX_SIZE, MAX_SIZE)
                        .onlyScaleDown()
                        .centerInside()
                        .into(it, callback)
                } catch (ex: Exception) {
                    InAppLogger(TAG).debug(ex, "Downloading image failed $imageUrl")
                }
            }
        }
    }

    private fun setBackground(width: Int, height: Int) {
        val imageView = findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)
        imageView.layoutParams.width = width + PADDING
        imageView.layoutParams.height = height + PADDING

        val shapePathModel = ShapeAppearanceModel.builder()
            .setAllCorners(CornerFamily.ROUNDED, RADIUS)
            .build()

        val backgroundDrawable = MaterialShapeDrawable(shapePathModel).apply {
            fillColor = ColorStateList.valueOf(Color.parseColor(bgColor))
        }

        imageView.background = backgroundDrawable
    }

    @SuppressWarnings("MagicNumber", "ComplexMethod")
    private fun setTip() {
        val tip = findViewById<ImageView>(R.id.message_tip)
        tip.layoutParams.height = TRI_SIZE
        tip.layoutParams.width = TRI_SIZE

        var adjustedWidth = TRI_SIZE
        var adjustedHeight = TRI_SIZE
        val close = findViewById<ImageButton>(R.id.message_close_button)
        if (close.drawable is ScaleDrawable) {
            close.drawable.level = 1
        }
        val imageView = findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)
        tip.setOnClickListener(this.listener)
        close.setOnClickListener(this.listener)
        imageView.setOnClickListener(this.listener)
        val ptArray = when (type) {
            PositionType.TOP_RIGHT -> {
                adjustedHeight = PADDING
                adjustedWidth = PADDING
                tip.layoutParams.height = PADDING
                tip.layoutParams.width = PADDING
                (tip.layoutParams as LayoutParams).addRule(ALIGN_START, R.id.message_tooltip_image_view)
                (tip.layoutParams as LayoutParams).addRule(ALIGN_BOTTOM, R.id.message_tooltip_image_view)
                (tip.layoutParams as MarginLayoutParams).leftMargin = -PADDING / 2
                (tip.layoutParams as MarginLayoutParams).bottomMargin = -PADDING / 2
                arrayOf(
                    Point(PADDING * 2 / 3, 0),
                    Point(0, PADDING),
                    Point(PADDING, PADDING * 1 / 3)
                )
            }
            PositionType.TOP_CENTER -> {
                (tip.layoutParams as LayoutParams).addRule(ALIGN_START, R.id.message_tooltip_image_view)
                (tip.layoutParams as LayoutParams).addRule(ALIGN_END, R.id.message_tooltip_image_view)
                (tip.layoutParams as LayoutParams).addRule(CENTER_HORIZONTAL)
                (tip.layoutParams as LayoutParams).addRule(BELOW, R.id.message_tooltip_image_view)

                arrayOf(
                    Point(0, 0),
                    Point(TRI_SIZE / 2, TRI_SIZE),
                    Point(TRI_SIZE, 0)
                )
            }
            PositionType.TOP_LEFT -> {
                alignLeft(close, tip)
                (tip.layoutParams as LayoutParams).addRule(ALIGN_BOTTOM, R.id.message_tooltip_image_view)
                (tip.layoutParams as MarginLayoutParams).rightMargin = -PADDING / 2
                (tip.layoutParams as MarginLayoutParams).bottomMargin = -PADDING / 2
                adjustedHeight = PADDING
                adjustedWidth = PADDING
                arrayOf(
                    Point(PADDING * 1 / 3, 0),
                    Point(PADDING, PADDING),
                    Point(0, PADDING * 1 / 3)
                )
            }
            PositionType.BOTTOM_RIGHT -> {
                adjustedHeight = PADDING
                adjustedWidth = PADDING
                tip.layoutParams.height = PADDING
                tip.layoutParams.width = PADDING
                (tip.layoutParams as LayoutParams).addRule(ALIGN_TOP, R.id.message_tooltip_image_view)
                (tip.layoutParams as MarginLayoutParams).leftMargin = -PADDING / 2
                (tip.layoutParams as MarginLayoutParams).topMargin = -PADDING / 2
                arrayOf(
                    Point(PADDING * 2 / 3, PADDING),
                    Point(0, 0),
                    Point(PADDING, PADDING * 2 / 3)
                )
            }
            PositionType.BOTTOM_CENTER -> {
                (findViewById<RelativeLayout>(R.id.image_layout).layoutParams as LayoutParams).removeRule(BELOW)
                (close.layoutParams as LayoutParams).addRule(BELOW, R.id.image_layout)
                (tip.layoutParams as LayoutParams).addRule(ALIGN_START, R.id.message_tooltip_image_view)
                (tip.layoutParams as LayoutParams).addRule(ALIGN_END, R.id.message_tooltip_image_view)
                (tip.layoutParams as LayoutParams).addRule(CENTER_HORIZONTAL)
                (imageView.layoutParams as LayoutParams).addRule(BELOW, R.id.message_tip)
                arrayOf(
                    Point(0, TRI_SIZE),
                    Point(TRI_SIZE / 2, 0),
                    Point(TRI_SIZE, TRI_SIZE)
                )
            }
            PositionType.BOTTOM_LEFT -> {
                alignLeft(close, tip)
                adjustedHeight = PADDING
                adjustedWidth = PADDING
                (tip.layoutParams as LayoutParams).addRule(ALIGN_TOP, R.id.message_tooltip_image_view)
                (tip.layoutParams as MarginLayoutParams).rightMargin = -PADDING / 2
                (tip.layoutParams as MarginLayoutParams).topMargin = -PADDING / 2
                arrayOf(
                    Point(0, PADDING * 2 / 3),
                    Point(PADDING, 0),
                    Point(PADDING * 1 / 3, PADDING)
                )
            }
            PositionType.RIGHT -> {
                (tip.layoutParams as LayoutParams).addRule(CENTER_VERTICAL)
                (imageView.layoutParams as LayoutParams).addRule(RIGHT_OF, R.id.message_tip)
                arrayOf(
                    Point(TRI_SIZE, 0),
                    Point(0, TRI_SIZE / 2),
                    Point(TRI_SIZE, TRI_SIZE)
                )
            }
            PositionType.LEFT -> {
                (close.layoutParams as LayoutParams).removeRule(END_OF)
                (close.layoutParams as LayoutParams).removeRule(ABOVE)
                (close.layoutParams as LayoutParams).removeRule(RIGHT_OF)
                (findViewById<RelativeLayout>(R.id.image_layout).layoutParams as LayoutParams)
                    .addRule(END_OF, R.id.message_close_button)
                (tip.layoutParams as LayoutParams).addRule(CENTER_VERTICAL)
                (tip.layoutParams as LayoutParams).addRule(RIGHT_OF, R.id.message_tooltip_image_view)
                arrayOf(
                    Point(0, 0),
                    Point(TRI_SIZE, TRI_SIZE / 2),
                    Point(0, TRI_SIZE)
                )
            }
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

        tip.setImageBitmap(bg)
    }

    private fun alignLeft(close: ImageButton, tip: ImageView) {
        (close.layoutParams as LayoutParams).removeRule(END_OF)
        (close.layoutParams as LayoutParams).removeRule(ABOVE)
        (close.layoutParams as LayoutParams).removeRule(RIGHT_OF)
        (findViewById<RelativeLayout>(R.id.image_layout).layoutParams as LayoutParams).addRule(
            END_OF,
            R.id.message_close_button
        )
        tip.layoutParams.height = PADDING
        tip.layoutParams.width = PADDING
        (tip.layoutParams as LayoutParams).addRule(ALIGN_END, R.id.message_tooltip_image_view)
    }

    @SuppressWarnings("NestedScopeFunctions")
    private fun showView() {
        val params = this.layoutParams as MarginLayoutParams
        (parent as ViewGroup).clipChildren = false
        (parent as ViewGroup).clipToPadding = false
        val imageView = findViewById<ImageView>(R.id.message_tooltip_image_view)
        viewId?.let { id ->
            val activity = InAppMessaging.instance().getRegisteredActivity() ?: return
            ResourceUtils.findViewByName<View>(activity, id)?.let { view ->
                val buttonSize = findViewById<ImageButton>(R.id.message_close_button).layoutParams.height
                ViewUtil.getPosition(
                    view = view, type = type, width = imageView.layoutParams.width,
                    height = imageView.layoutParams.height, marginH = buttonSize, marginV = buttonSize
                )
                    .let { pos ->
                        params.topMargin = pos.second
                        params.leftMargin = pos.first
                    }
            }
        }
    }

    internal fun setPosition(params: MarginLayoutParams, pos: Pair<Int, Int>, width: Int, height: Int) {
        params.topMargin = pos.second
        params.leftMargin = pos.first
        val edgePos = ViewUtil.getEdgePosition(width, height, pos)
        edgePos.first?.let { right ->
            params.rightMargin = right
        }
        edgePos.second?.let { bottom ->
            params.bottomMargin = bottom
        }
    }

    companion object {
        private const val TAG = "IAM_ToolTipView"
        internal const val PADDING = 40
        internal const val TRI_SIZE = 20
        private const val RADIUS = 15f
        private const val MAX_SIZE = 600
        private const val DELAY = 100L // in ms
    }
}
