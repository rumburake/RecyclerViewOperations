package com.threecats.swiperecyclerview

import android.animation.ArgbEvaluator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
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

            val myCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

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
                    c.clipRect(0f, viewHolder.itemView.top.toFloat(), dX, viewHolder.itemView.bottom.toFloat())
                    val width = viewHolder.itemView.width.toFloat()
                    val limit = width * 0.7f
                    val color = if (dX >= limit) {
                        Color.RED
                    } else {
                        ArgbEvaluator().evaluate(dX / limit, Color.GRAY, Color.RED) as Int
                    }
                    c.drawColor(color)

                    val trashIcon = resources.getDrawable(R.drawable.ic_baseline_delete_24, null)
                    val margin = resources.getDimension(R.dimen.text_margin).roundToInt()
                    trashIcon.bounds = Rect(
                        margin,
                        margin + viewHolder.itemView.top,
                        margin + trashIcon.intrinsicWidth,
                        margin + viewHolder.itemView.top + trashIcon.intrinsicHeight
                    )
                    trashIcon.draw(c)
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