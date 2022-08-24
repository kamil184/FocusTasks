package com.kamil184.focustasks.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kamil184.focustasks.R
import com.kamil184.focustasks.model.CalendarMonthHelper
import java.util.*

class CalendarViewPagerAdapter : RecyclerView.Adapter<CalendarViewPagerAdapter.ViewHolder>() {

    private val calendarMonthHelper = CalendarMonthHelper()

    inner class ViewHolder(private val recyclerView: RecyclerView) :
        RecyclerView.ViewHolder(recyclerView) {
        fun bind(calendar: Calendar) {
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 7)
            recyclerView.adapter = CalendarViewAdapter(calendarMonthHelper.getDaysOfMonth(calendar), calendarMonthHelper)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val recyclerView =
            inflater.inflate(R.layout.date_picker_calendar_recycler, parent, false) as RecyclerView
        return ViewHolder(recyclerView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(CalendarMonthHelper.allMonths[position])
    }

    override fun getItemCount() = CalendarMonthHelper.allMonths.size
}