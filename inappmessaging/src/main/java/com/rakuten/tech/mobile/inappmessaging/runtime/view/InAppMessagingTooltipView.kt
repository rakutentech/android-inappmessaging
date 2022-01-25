package com.rakuten.tech.mobile.inappmessaging.runtime.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.media.Image
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.core.widget.NestedScrollView
import com.rakuten.tech.mobile.inappmessaging.runtime.InAppMessaging
import com.rakuten.tech.mobile.inappmessaging.runtime.R
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.PositionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.messages.Message
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ContextExtension.findViewByName
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ViewUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import timber.log.Timber
import kotlin.math.abs
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.TriangleEdgeTreatment


internal class InAppMessagingTooltipView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(context, attrs), InAppMessageView {

    init {
        id = R.id.in_app_message_tooltip_view
    }

    private var imageUrl: String? = null
    private var bgColor = "#FFFFFF" // default white
    private var type: PositionType = PositionType.BOTTOM_CENTER
    private var viewId: String? = null

    @VisibleForTesting
    internal var picasso: Picasso? = null

    override fun populateViewData(message: Message) {
        this.imageUrl = message.getMessagePayload().resource.imageUrl
        this.bgColor = message.getMessagePayload().backgroundColor
        message.getTooltipConfig()?.let {
            PositionType.getById(it.position)?.let { posType ->
                type = PositionType.BOTTOM_CENTER
            }
            viewId = it.id
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
                            this@InAppMessagingTooltipView.visibility = INVISIBLE
                        }

                        override fun onError(e: Exception?) {
                            Timber.tag(TAG).d(e?.cause, "Downloading image failed $imageUrl")
                        }
                    }

//                    it.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
//                        override fun onPreDraw(): Boolean {
//                            if (it.width > 0) {
//                                setBackground(it.width, it.height)
//                                setTip(it.width, it.height)
//                                it.viewTreeObserver.removeOnPreDrawListener(this)
//                                showView()
//                                this@InAppMessagingTooltipView.visibility = VISIBLE
//                            }
//                            return true
//                        }
//                    })

                    it.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (it.width > 0) {
                                setBackground(it.width, it.height)
                                setTip(it.width, it.height)
                                it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                showView()
                                this@InAppMessagingTooltipView.visibility = VISIBLE
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
                    Timber.tag(TAG).d(ex, "Downloading image failed $imageUrl")
                }
            }
        }
    }

    private fun setBackground(width: Int, height: Int) {

        val imageView = findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)
//        imageView.setBackgroundColor(Color.parseColor(bgColor))
        imageView.layoutParams.width = width + PADDING
        imageView.layoutParams.height = height + PADDING

        val shapePathModel = ShapeAppearanceModel.builder()
            .setAllCorners(CornerFamily.ROUNDED, 15.toFloat())
            .build()

        val backgroundDrawable = MaterialShapeDrawable(shapePathModel).apply {
            fillColor = ColorStateList.valueOf(Color.parseColor(bgColor))
        }

        imageView.background = backgroundDrawable

//        tip.setBackgroundColor(Color.parseColor(bgColor))
//        tip.background = tipDrawable

//        val paint = Paint()
//        val shadow = Paint()
//        paint.color = Color.parseColor(bgColor)//Color.parseColor("#A5000000")//Color.parseColor(bgColor)
//        shadow.color = Color.parseColor("#00FFFFFF")
//        val bg = Bitmap.createBitmap(width + PADDING, height + PADDING, Bitmap.Config.ARGB_8888)
//        val bgShadow = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bg)
//        val canvasShadow = Canvas(bgShadow)
//
//        val path = Path()
//        val corners = floatArrayOf(RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS)
//        path.addRoundRect(
//            RectF(0f, 0f, (width + PADDING).toFloat(), (height + PADDING).toFloat()),
//            corners,
//            Path.Direction.CW
//        )
//        canvas.drawPath(path, paint)
//        canvasShadow.drawPath(path, shadow)
//        val imageView = findViewById<ImageView>(R.id.message_background)
//        imageView.setImageBitmap(bg)
    }

    private fun setTip(width: Int, height: Int) {
        val tip = findViewById<ImageView>(R.id.message_tip)
        tip.layoutParams.height = TRI_SIZE
        tip.layoutParams.width = TRI_SIZE

        var adjustedWidth = TRI_SIZE
        var adjustedHeight = TRI_SIZE
        val close = findViewById<ImageView>(R.id.message_close_button)
        val imageView = findViewById<ShapeableImageView>(R.id.message_tooltip_image_view)
        val ptArray = when (type) {
            PositionType.TOP_RIGHT -> {
                adjustedHeight = PADDING
                adjustedWidth = PADDING
//                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(TRI_SIZE, 0, 0, TRI_SIZE)
//                findViewById<ImageView>(R.id.message_background)?.setPadding(TRI_SIZE, 0, 0, TRI_SIZE)
//                findViewById<ImageView>(R.id.message_shadow)?.setPadding(TRI_SIZE, 0, 0, TRI_SIZE)
//                (close?.layoutParams as MarginLayoutParams).leftMargin =
//                    context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_left) + TRI_SIZE/2
//                (close.layoutParams as MarginLayoutParams).bottomMargin =
//                    context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_bottom) + TRI_SIZE/2
//                close.requestLayout()
                tip.layoutParams.height = PADDING
                tip.layoutParams.width = PADDING
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_START, R.id.message_tooltip_image_view)
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_BOTTOM, R.id.message_tooltip_image_view)
                (tip?.layoutParams as MarginLayoutParams?)?.leftMargin = -PADDING/2
                (tip?.layoutParams as MarginLayoutParams?)?.bottomMargin = -PADDING/2
                tip.requestLayout()
                arrayOf(
                    Point(PADDING * 2 / 3, 0),
                    Point(0, PADDING),
                    Point(PADDING, PADDING * 1 / 3)
                )
            }
            PositionType.TOP_CENTER -> {
//                adjustedHeight += PADDING
//                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(0, 0, 0, TRI_SIZE + TRI_SIZE/4)
//                findViewById<ImageView>(R.id.message_background)?.setPadding(0, 0, 0, TRI_SIZE + TRI_SIZE/4)
//                findViewById<ImageView>(R.id.message_shadow)?.setPadding(0, 0, 0, PADDING)
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_START, R.id.message_tooltip_image_view)
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_END, R.id.message_tooltip_image_view)
                (tip?.layoutParams as LayoutParams?)?.addRule(CENTER_HORIZONTAL)
                (tip?.layoutParams as LayoutParams?)?.addRule(BELOW, R.id.message_tooltip_image_view)
                arrayOf(
                    Point(0, 0),
                    Point(TRI_SIZE / 2, TRI_SIZE),
                    Point(TRI_SIZE, 0)
                )
            }
            PositionType.TOP_LEFT -> {
                (close?.layoutParams as LayoutParams?)?.removeRule(END_OF)
                (close?.layoutParams as LayoutParams?)?.addRule(START_OF, R.id.message_tooltip_image_view)
                (close?.layoutParams as MarginLayoutParams).leftMargin = context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_left)*3
                adjustedHeight = PADDING
                adjustedWidth = PADDING
                tip.layoutParams.height = PADDING
                tip.layoutParams.width = PADDING
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_END, R.id.message_tooltip_image_view)
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_BOTTOM, R.id.message_tooltip_image_view)
                (tip?.layoutParams as MarginLayoutParams?)?.rightMargin = -PADDING/2
                (tip?.layoutParams as MarginLayoutParams?)?.bottomMargin = -PADDING/2
                tip.requestLayout()
                arrayOf(
                    Point(PADDING * 1 / 3, 0),
                    Point(PADDING, PADDING),
                    Point(0, PADDING * 1 / 3)
                )
            }
            PositionType.BOTTOM_RIGHT -> {
//                adjustedHeight += PADDING
//                findViewById<ImageView>(R.id.message_shadow)?.setPadding(TRI_SIZE, TRI_SIZE, 0, 0)
                adjustedHeight = PADDING
                adjustedWidth = PADDING
                tip.layoutParams.height = PADDING
                tip.layoutParams.width = PADDING
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_TOP, R.id.message_tooltip_image_view)
                (tip?.layoutParams as MarginLayoutParams?)?.leftMargin = -PADDING/2
                (tip?.layoutParams as MarginLayoutParams?)?.topMargin = -PADDING/2
                tip.requestLayout()
                arrayOf(
                    Point(PADDING * 2 / 3, PADDING),
                    Point(0, 0),
                    Point(PADDING, PADDING * 2 / 3)
                )
            }
            PositionType.BOTTOM_CENTER -> {
//                adjustedHeight += PADDING
//                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(0, TRI_SIZE + TRI_SIZE/4, 0, 0)
//                findViewById<ImageView>(R.id.message_background)?.setPadding(0, TRI_SIZE + TRI_SIZE/4, 0, 0)
//                findViewById<ImageView>(R.id.message_shadow)?.setPadding(0, TRI_SIZE + TRI_SIZE/4, 0, 0)
                (close?.layoutParams as LayoutParams?)?.removeRule(ABOVE)
                (close?.layoutParams as LayoutParams?)?.addRule(BELOW, R.id.message_tooltip_image_view)
                (close?.layoutParams as MarginLayoutParams).topMargin = context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_bottom)*2
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_START, R.id.message_tooltip_image_view)
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_END, R.id.message_tooltip_image_view)
                (tip?.layoutParams as LayoutParams?)?.addRule(CENTER_HORIZONTAL)
                (imageView?.layoutParams as LayoutParams?)?.addRule(BELOW, R.id.message_tip)
                arrayOf(
                    Point(0, TRI_SIZE),
                    Point(TRI_SIZE / 2, 0),
                    Point(TRI_SIZE, TRI_SIZE)
                )
            }
            PositionType.BOTTOM_LEFT -> {
//                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(0, TRI_SIZE, TRI_SIZE, 0)
//                findViewById<ImageView>(R.id.message_background)?.setPadding(0, TRI_SIZE, TRI_SIZE, 0)
//                findViewById<ImageView>(R.id.message_shadow)?.setPadding(0, TRI_SIZE, TRI_SIZE, 0)
                (close?.layoutParams as LayoutParams?)?.removeRule(END_OF)
                (close?.layoutParams as LayoutParams?)?.addRule(START_OF, R.id.message_tooltip_image_view)
                (close?.layoutParams as MarginLayoutParams).leftMargin = context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_left)*3
                adjustedHeight = PADDING
                adjustedWidth = PADDING
                tip.layoutParams.height = PADDING
                tip.layoutParams.width = PADDING
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_END, R.id.message_tooltip_image_view)
                (tip?.layoutParams as LayoutParams?)?.addRule(ALIGN_TOP, R.id.message_tooltip_image_view)
                (tip?.layoutParams as MarginLayoutParams?)?.rightMargin = -PADDING/2
                (tip?.layoutParams as MarginLayoutParams?)?.topMargin = -PADDING/2
                tip.requestLayout()
                arrayOf(
                    Point(0, PADDING * 2 / 3),
                    Point(PADDING, 0),
                    Point(PADDING * 1 / 3, PADDING)
                )
            }
            PositionType.RIGHT -> {
//                adjustedWidth += PADDING
//                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(PADDING, 0, 0, 0)
//                findViewById<ImageView>(R.id.message_background)?.setPadding(PADDING, 0, 0, 0)
//                findViewById<ImageView>(R.id.message_shadow)?.setPadding(PADDING, 0, 0, 0)
//                (close?.layoutParams as MarginLayoutParams).leftMargin =
//                    context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_left) + TRI_SIZE/2
//                (close.layoutParams as MarginLayoutParams).bottomMargin =
//                    context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_bottom) + TRI_SIZE/2
//                close.requestLayout()
                (tip?.layoutParams as LayoutParams?)?.addRule(CENTER_VERTICAL)
                (imageView?.layoutParams as LayoutParams?)?.addRule(RIGHT_OF, R.id.message_tip)
//                imageView.requestLayout()
                arrayOf(
                    Point(TRI_SIZE, 0),
                    Point(0, TRI_SIZE / 2),
                    Point(TRI_SIZE, TRI_SIZE)
                )
            }
            PositionType.LEFT -> {
//                adjustedWidth += PADDING
//                findViewById<ImageView>(R.id.message_tooltip_image_view)?.setPadding(0, 0, PADDING, 0)
//                findViewById<ImageView>(R.id.message_background)?.setPadding(0, 0, PADDING, 0)
//                findViewById<ImageView>(R.id.message_shadow)?.setPadding(0, 0, PADDING, 0)
                (close?.layoutParams as LayoutParams?)?.removeRule(END_OF)
                (close?.layoutParams as LayoutParams?)?.addRule(START_OF, R.id.message_tooltip_image_view)
                (close?.layoutParams as MarginLayoutParams).leftMargin = context.resources.getDimensionPixelSize(R.dimen.tooltip_close_button_margin_left)*3
                (tip?.layoutParams as LayoutParams?)?.addRule(CENTER_VERTICAL)
                (tip?.layoutParams as LayoutParams?)?.addRule(RIGHT_OF, R.id.message_tooltip_image_view)
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
        paint.color = Color.parseColor(bgColor) //Color.BLACK//
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        val bg = Bitmap.createBitmap(adjustedWidth, adjustedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bg)
        canvas.drawPath(path, paint)

        tip.setImageBitmap(bg)
    }

    private fun showView() {
        val params = this.layoutParams as MarginLayoutParams
        clipChildren = false
        clipToPadding = false
        (parent as ViewGroup).clipChildren = false
        (parent as ViewGroup).clipToPadding = false
        val imageView = findViewById<ImageView>(R.id.message_tooltip_image_view)
        viewId?.let {
            InAppMessaging.instance().getRegisteredActivity()?.findViewByName<View>(it)?.let { view ->
//                val layout = findViewById<RelativeLayout>(R.id.tooltip_layout)
                val horizontal = abs(context.resources.getDimension(R.dimen.tooltip_close_button_margin_left)).toInt()
                val vertical = abs(context.resources.getDimension(R.dimen.tooltip_close_button_margin_bottom)).toInt()
                val buttonSize = context.resources.getDimension(R.dimen.modal_close_button_height).toInt()
//                ViewUtil.isInScrollView(view)
                ViewUtil.isInScrollView(this)
                ViewUtil.getLayoutPosition(view, type, imageView.layoutParams.width, imageView.layoutParams.height,buttonSize - horizontal, buttonSize - vertical)?.let { pos ->
                    val rect = Rect()
                    view.getHitRect(rect)
                    params.topMargin = pos.second
                    params.leftMargin = pos.first
//                    params.topMargin = rect.top
//                    params.leftMargin = rect.left
                    this.requestLayout()
                }
            }
        }
        invalidate()
    }

    companion object {
        private const val TAG = "IAM_ToolTipView"
        internal const val PADDING = 40
        internal const val TRI_SIZE = 20
        private const val RADIUS = 20f
        private const val MAX_SIZE = 600
    }
}
