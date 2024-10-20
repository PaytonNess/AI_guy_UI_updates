package com.example.alguardianguyproject

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alguardianguyproject.RecordConstants.DIMENSION_OPTIONS
import kotlin.math.abs

class ResizableOverlayView @JvmOverloads constructor(
    context: Context,
    private val windowManager: WindowManager,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = INVALID_POINTER_ID
    var recording = false

    private val minWidth = 400
    private val minHeight = 400

    private lateinit var startButton: Button
    private lateinit var spinner: Spinner

    private lateinit var buttonContainer: StartButtonView
    private lateinit var chatContainer: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var closeContainer: CloseButtonView
    private var gridView: GridView
    private lateinit var menuContainer: LinearLayout
    lateinit var menuHomeView: MenuHomeView
    lateinit var menuChatView: MenuChatView
    private lateinit var darkOverlay: LinearLayout

    private lateinit var chatRecyclerView: RecyclerView
    lateinit var messageInput: EditText
    lateinit var sendButton: Button
    lateinit var closeButton: Button
//    TODO: comment for production
    lateinit var modelTextField: EditText
    lateinit var modelSpinner: Spinner

    lateinit var recyclerView: RecyclerView
    lateinit var chatButton: Button
    var chatOpen = false

    lateinit var menuButton: Button

    init {

        // Set transparent background with border
        background = null
        //setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
        //background = ResourcesCompat.getDrawable(resources, R.drawable.blank, null)
        minimumWidth = minWidth
        minimumHeight = minHeight
        isClickable = false
        isFocusable = false
        isFocusableInTouchMode = false
        isLongClickable = false
        isContextClickable = false
        clipChildren = false
        clipToOutline = false
        clipToPadding = false

        Log.d("OverlayView", "isClickable: $isClickable, isFocusable: $isFocusable, " +
                "isFocusableInTouchMode: $isFocusableInTouchMode, isLongClickable: $isLongClickable, " +
                "isContextClickable: $isContextClickable, background: $background")
        // Add buttons to the view
        gridView = GridView(context)
        val customBackground = LayoutInflater.from(context).inflate(R.layout.corner_border, this, false)
        addView(gridView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.TOP or Gravity.START
            width = LayoutParams.MATCH_PARENT
            height = LayoutParams.MATCH_PARENT
            x = 0f
            y = 0f
        })
        gridView.addSizeOffset(400f, 500f)
        addView(customBackground, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.BOTTOM
        })
        initChatComponents(context)
        initRecordComponents(context)
        initLoadingComponents(context)
        initCloseButton(context)
        initMenuComponents(context)
        initDarkOverlay(context)

        addCloseButton()
        addMenuComponents()
    }

    private fun initMenuComponents(context: Context) {
        menuContainer = LinearLayout(context)
        val inflater = LayoutInflater.from(context).inflate(R.layout.menu_button, menuContainer, true)
        menuButton = inflater.findViewById(R.id.button)

        menuHomeView = MenuHomeView(context)
        menuChatView = MenuChatView(context)

        spinner = menuHomeView.findViewById(R.id.my_spinner)

        val yourDataList = resources.getStringArray(R.array.dropdown_options)
        val displayDataList = yourDataList.map { "Media Type: $it" }
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(
            context,
            R.layout.spinner_list,
            displayDataList
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_list)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        // Set a listener for the selected option
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                Toast.makeText(context, "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
                if (position > 0 && position < DIMENSION_OPTIONS.size) {
                    val dimension = DIMENSION_OPTIONS[position]
                    val displayMetrics = resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val screenHeight = displayMetrics.heightPixels
                    val density = displayMetrics.density
                    val width = screenWidth * dimension.width
                    val height = screenWidth * dimension.height + 54f * density
                    val x = screenWidth * dimension.x
                    val y = screenHeight * dimension.y - 27f * density
                    setOverlayTransform(width, height, x, y)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing here
            }
        }
    }

    fun addMenuComponents() {
        addView(menuContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.TOP or Gravity.START
        })
    }

    fun removeMenuComponents() {
        removeView(menuContainer)
    }

    fun addMenuHomeView() {
        addDarkOverlay()
        addView(menuHomeView)
        menuHomeView.bringToFront()
    }

    fun removeMenuHomeView() {
        removeView(menuHomeView)
        removeDarkOverlay()
    }

    fun addMenuChatView() {
        addDarkOverlay()
        addView(menuChatView)
        menuChatView.bringToFront()
    }

    fun removeMenuChatView() {
        removeView(menuChatView)
        removeDarkOverlay()
    }

    fun setMenuButtonToClosedState() {
        menuHomeView.opened = false
        menuButton.setText(R.string.menu)
        menuButton.setBackgroundResource(R.drawable.menu_closed)
    }

    fun setMenuButtonToOpenState() {
        menuHomeView.opened = true
        menuButton.text = null
        menuButton.setBackgroundResource(R.drawable.menu_opened)
    }

    private fun initChatComponents(context: Context) {
        chatContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val inflater = LayoutInflater.from(context).inflate(R.layout.activity_chat, chatContainer, true)
        chatRecyclerView = inflater.findViewById(R.id.message_list)
        chatRecyclerView.layoutManager = LinearLayoutManager(context)
        messageInput = inflater.findViewById(R.id.message_input)
        sendButton = inflater.findViewById(R.id.send_button)
//        TODO: comment for production
        modelTextField = inflater.findViewById(R.id.editText)

        modelSpinner = inflater.findViewById(R.id.model_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            context,
            R.array.model_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            modelSpinner.adapter = adapter
        }
    }

    private fun initDarkOverlay(context: Context) {
        darkOverlay = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        LayoutInflater.from(context).inflate(R.layout.dark_overlay, darkOverlay, true)
    }

    private fun addDarkOverlay() {
        addView(darkOverlay, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.BOTTOM
        })
    }

    private fun removeDarkOverlay() {
        removeView(darkOverlay)
    }

    private fun initCloseButton(context: Context) {
        closeContainer = CloseButtonView(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        closeButton = closeContainer.closeButton
    }

    private fun addCloseButton() {
        addView(closeContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.END
        })
        closeContainer.bringToFront()
    }

    fun addChatComponents() {
        addView(chatContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.BOTTOM
        })
        closeContainer.bringToFront()
        chatOpen = true
    }

    fun removeChatComponents() {
        removeView(chatContainer)
        chatOpen = false
    }

    fun removeLoadingComponents() {
        removeView(loadingContainer)
    }

    fun removeRecordComponents() {
        removeView(buttonContainer)
    }

    private fun initLoadingComponents(context: Context) {
        loadingContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val inflater = LayoutInflater.from(context).inflate(R.layout.activity_record, loadingContainer, true)
        recyclerView = inflater.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        chatButton = inflater.findViewById(R.id.chatButton)
    }

    fun addLoadingComponents() {
        addView(loadingContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP
        })
        closeContainer.bringToFront()
    }

    // Add a method to set the adapter for the RecyclerView
    fun setChatAdapter(adapter: RecyclerView.Adapter<*>) {
        chatRecyclerView.adapter = adapter
    }

    private fun initRecordComponents(context: Context) {
        buttonContainer = StartButtonView(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        startButton = buttonContainer.startButton
        startButton.text = context.getString(R.string.record)
    }

    fun addRecordComponents() {
        // Add button container to the FrameLayout
        addView(buttonContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        })
        closeContainer.bringToFront()
        spinner.visibility = View.VISIBLE
    }

    fun switchToStopButton() {
        startButton.text = context.getString(R.string.stop)
        spinner.visibility = View.GONE
    }

    fun switchToStartButton() {
        startButton.text = context.getString(R.string.record)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Log.d("OverlayTouch", "onTouchEvent: action=${event.actionMasked}, x=${event.x}, y=${event.y}")
//        if (recording) {
//            println("recording")
//            val startButtonRect = Rect()
//            startButton.getHitRect(startButtonRect)
//            val closeButtonRect = Rect()
//            closeButton.getHitRect(closeButtonRect)
//
//            if (startButtonRect.contains(event.x.toInt(), event.y.toInt()) || closeButtonRect.contains(event.x.toInt(), event.y.toInt())
//                ) {
//                    return super.onTouchEvent(event) // Handle the touch event normally for buttons
//                } else {
//                    println("ignored touch in resizable overlay view")
//                    return false // Ignore touch events outside of buttons
//                }
//        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId != INVALID_POINTER_ID) {
                    val x = event.rawX
                    val y = event.rawY
                    val center = getCenterCoordinates()
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    if (abs(dx) > 5 || abs(dy) > 5) {
                        val params = layoutParams as WindowManager.LayoutParams

                        if (isInResizeRange(x - center.first, y - center.second)) {
                            // Resize the view
                            params.width = (width + dx).toInt().coerceAtLeast(minWidth)
                            params.height = (height + dy).toInt().coerceAtLeast(minHeight)
                            gridView.addSizeOffset(dx, dy)
                        }
                        else {
                            // Move the view
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            gridView.addOffset(dx, dy)
                        }

                        menuHomeView.hideButtons(params.height)
                        menuChatView.hideButtons(params.height)

                        windowManager.updateViewLayout(this, params)

                        lastTouchX = x
                        lastTouchY = y
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }
        }
        return true
    }

    private fun isInResizeRange(x: Float, y: Float): Boolean {
        val threshold = 50 // Increase threshold for easier resizing
        return (x > width / 2 - threshold || y > height / 2 - threshold)
    }

    private fun getCenterCoordinates(): Pair<Float, Float> {
        val location = IntArray(2)
        getLocationOnScreen(location)

        val x = location[0] + width / 2
        val y = location[1] + height / 2

        // Now you have the x and y coordinates of the view
        return x.toFloat() to y.toFloat()
    }

    private fun setOverlayTransform(width: Double, height: Double, x: Double, y: Double) {
        val params = layoutParams as WindowManager.LayoutParams
        params.width = width.toInt()
        params.height = height.toInt()
        params.x = x.toInt()
        params.y = y.toInt()
        gridView.addSizeOffset(width.toFloat() - gridView.width.toFloat(), height.toFloat() - gridView.height.toFloat())
        gridView.addOffset(x.toFloat() - gridView.x, y.toFloat() - gridView.y)
        windowManager.updateViewLayout(this, params)

        menuHomeView.hideButtons(params.height)
        menuChatView.hideButtons(params.height)
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}
