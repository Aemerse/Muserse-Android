package com.aemerse.muserse.uiElementHelper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class BottomOffsetDecoration constructor(private val mBottomOffset: Int) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val dataSize: Int = state.itemCount
        val position: Int = parent.getChildAdapterPosition(view)
        when {
            dataSize > 0 && position == dataSize - 1 -> {
                outRect.set(0, 0, 0, mBottomOffset)
            }
            else -> {
                outRect.set(0, 0, 0, 0)
            }
        }
    }
}