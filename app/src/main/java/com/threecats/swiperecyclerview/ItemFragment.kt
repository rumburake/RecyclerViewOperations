package com.threecats.swiperecyclerview

import android.animation.ArgbEvaluator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.threecats.swiperecyclerview.dummy.DummyContent
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A fragment representing a list of Items.
 */
class ItemFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val swipeView = inflater.inflate(R.layout.fragment_item_list, container, false)
            as SwipeRefreshLayout
        val recyclerView = swipeView.findViewById<RecyclerView>(R.id.list_view)

        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = MyItemRecyclerViewAdapter(DummyContent.ITEMS)

            val stopwatch = Stopwatch()

            var dragInitialPosition = RecyclerView.NO_POSITION

            val myCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END) {

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    if (dragInitialPosition != RecyclerView.NO_POSITION) {
                        val dragFinalPosition = viewHolder.adapterPosition
                        if (dragFinalPosition != dragInitialPosition) {
                            Log.d("Drag", "Done dragging. Moved from: $dragInitialPosition to $dragFinalPosition.")
                        } else {
                            Log.d("Drag", "Done dragging. Item not moved.")
                        }
                        dragInitialPosition = RecyclerView.NO_POSITION
                    }
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val start = viewHolder.adapterPosition
                    val end = target.adapterPosition

                    if (dragInitialPosition == RecyclerView.NO_POSITION) {
                        dragInitialPosition = start
                    }

                    if (start < end) {
                        for (i in start until end) {
                            Collections.swap(DummyContent.ITEMS, i, i + 1)
                        }
                    } else {
                        for (i in start downTo end + 1) {
                            Collections.swap(DummyContent.ITEMS, i, i - 1)
                        }
                    }
                    adapter?.notifyItemMoved(start, end)
                    // Thread.sleep(50)
                    Log.d("stopwatch", "Since last: ${stopwatch.lapMs()}")
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    DummyContent.ITEMS.removeAt(viewHolder.adapterPosition)
                    adapter?.notifyItemRemoved(viewHolder.adapterPosition)
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val width = viewHolder.itemView.width.toFloat()
                        val threshold = width * 0.7f
                        val move = abs(dX)

                        // set stage
                        if (dX >= 0) { // left to right
                            c.clipRect(
                                0f,
                                viewHolder.itemView.top.toFloat(),
                                dX,
                                viewHolder.itemView.bottom.toFloat()
                            )
                        } else { // right to left
                            c.clipRect(
                                viewHolder.itemView.right.toFloat() - move,
                                viewHolder.itemView.top.toFloat(),
                                viewHolder.itemView.right.toFloat(),
                                viewHolder.itemView.bottom.toFloat()
                            )
                        }

                        // gradual color
                        val colorBase = if (dX >= 0) Color.RED else Color.BLUE
                        val color = if (move >= threshold) {
                            colorBase
                        } else {
                            ArgbEvaluator().evaluate(move / threshold, Color.GRAY, colorBase) as Int
                        }
                        c.drawColor(color)

                        val trashIcon = resources.getDrawable(R.drawable.ic_baseline_delete_24, null)
                        val margin = resources.getDimension(R.dimen.text_margin).roundToInt()

                        trashIcon.bounds = if (dX >= 0) {
                            Rect(
                                margin,
                                margin + viewHolder.itemView.top,
                                margin + trashIcon.intrinsicWidth,
                                margin + viewHolder.itemView.top + trashIcon.intrinsicHeight
                            )
                        } else {
                            Rect(
                                viewHolder.itemView.right - margin - trashIcon.intrinsicWidth,
                                viewHolder.itemView.top + margin,
                                viewHolder.itemView.right - margin,
                                viewHolder.itemView.top + margin + trashIcon.intrinsicHeight
                            )
                        }
                        trashIcon.draw(c)
                    } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG){
                        val height = viewHolder.itemView.height.toFloat()
                        val move = if (abs(dY) > height) height else abs(dY)

                        // set stage
                        if (dY >= 0) { // downwards
                            c.clipRect(
                                0f,
                                viewHolder.itemView.top.toFloat(),
                                viewHolder.itemView.right.toFloat(),
                                viewHolder.itemView.top.toFloat() + move
                            )
                        } else { // upwards
                            c.clipRect(
                                0f,
                                viewHolder.itemView.bottom.toFloat() - move,
                                viewHolder.itemView.right.toFloat(),
                                viewHolder.itemView.bottom.toFloat()
                            )
                        }

                        // gradual color (although is distracting here)
                        val colorBase = if (dY >= 0) Color.YELLOW else Color.GREEN
                        val color = ArgbEvaluator().evaluate(move / height, Color.GRAY, colorBase) as Int
                        c.drawColor(color)

//                        c.drawColor(Color.LTGRAY)
                    }
                }
            }

            val myHelper = ItemTouchHelper(myCallback)
            myHelper.attachToRecyclerView(this)

            swipeView.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
                DummyContent.generate()
                adapter?.notifyDataSetChanged()
                swipeView.isRefreshing = false
            })
        }

        return swipeView
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}