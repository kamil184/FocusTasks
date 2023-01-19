package com.kamil184.focustasks.ui.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.CalendarMonthsHelper
import com.kamil184.focustasks.data.model.SelectedDay
import java.util.*

class CalendarViewPagerAdapter(calendar: Calendar) : RecyclerView.Adapter<CalendarViewPagerAdapter.ViewHolder>() {
    constructor() : this(Calendar.getInstance())

    val selectedDay = SelectedDay(calendar)

    inner class ViewHolder(private val recyclerView: RecyclerView) :
        RecyclerView.ViewHolder(recyclerView) {
        fun bind(calendar: Calendar) {
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 7)
            recyclerView.adapter = CalendarViewAdapter(calendar, selectedDay)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val recyclerView =
            inflater.inflate(R.layout.date_picker_calendar_recycler, parent, false) as RecyclerView
        return ViewHolder(recyclerView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(CalendarMonthsHelper.allMonthsIn21Century[position])
    }

    override fun getItemCount() = CalendarMonthsHelper.allMonthsIn21Century.size
}