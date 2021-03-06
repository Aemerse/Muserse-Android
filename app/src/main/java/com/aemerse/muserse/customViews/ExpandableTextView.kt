package com.aemerse.muserse.customViews

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import com.aemerse.muserse.R

/**
 * User: Bazlur Rahman Rokon
 * Date: 9/7/13 - 3:33 AM
 */
class ExpandableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    private var originalText: CharSequence? = null
    private var trimmedText: CharSequence? = null
    private var bufferType: BufferType? = null
    private var trim: Boolean = true
    private var trimLength: Int

    private fun setText() {
        super.setText(getDisplayableText(), bufferType)
    }

    private fun getDisplayableText(): CharSequence? {
        return if (trim) trimmedText else originalText
    }

    override fun setText(text: CharSequence, type: BufferType) {
        originalText = text
        trimmedText = getTrimmedText(text)
        bufferType = type
        setText()
    }

    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        return when {
            originalText != null && originalText!!.length > trimLength -> {
                SpannableStringBuilder(originalText, 0, trimLength + 1).append(ELLIPSIS)
            }
            else -> {
                originalText
            }
        }
    }

    fun getOriginalText(): CharSequence? {
        return originalText
    }

    fun setTrimLength(trimLength: Int) {
        this.trimLength = trimLength
        trimmedText = getTrimmedText(originalText)
        setText()
    }

    fun getTrimLength(): Int {
        return trimLength
    }

    companion object {
        private const val DEFAULT_TRIM_LENGTH: Int = 200
        private const val ELLIPSIS: String = "....."
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH)
        typedArray.recycle()
        setOnClickListener {
            trim = !trim
            setText()
            requestFocusFromTouch()
        }
    }
}