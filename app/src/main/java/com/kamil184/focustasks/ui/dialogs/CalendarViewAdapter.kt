package com.kamil184.focustasks.ui.dialogs

import android.content.res.TypedArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kamil184.focustasks.R
import com.kamil184.focustasks.model.CalendarDay
import com.kamil184.focustasks.model.CalendarMonthHelper

class CalendarViewAdapter(private val calendarMonthHelper: CalendarMonthHelper) :
    RecyclerView.Adapter<CalendarViewAdapter.ViewHolder>() {
    private val days = calendarMonthHelper.getDaysOfMonth()

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        private var calendarDay: CalendarDay? = null

        init {
            textView.setOnClickListener {
                if (calendarDay != null && calendarDay?.isSelected == false) {
                    calendarDay!!.isSelected = true
                    updateTextColorAndBackground()
                    val lastSelectedId = calendarMonthHelper.onSelectedDayChange(calendarDay!!.calendar)
                    days[lastSelectedId]?.isSelected = false
                    notifyItemChanged(lastSelectedId)
                }
            }
        }

        fun bind(calendarDay: CalendarDay?) {
            this.calendarDay = calendarDay
            if (calendarDay != null) {
                textView.text = calendarDay.getText()
                updateTextColorAndBackground()
            }
        }

        private fun updateTextColorAndBackground() {
            if (calendarDay!!.isSelected) {
                textView.setBackgroundResource(R.drawable.date_picker_selected_day_background)
                textView.setTextColor(getColor(android.R.color.white))
            } else {
                when (calendarDay!!.type) {
                    CalendarDay.Type.TODAY -> {
                        textView.setBackgroundResource(android.R.color.transparent)
                        textView.setTextColor(getColor(R.attr.colorPrimary))
                    }
                    CalendarDay.Type.NORMAL -> {
                        textView.setBackgroundResource(android.R.color.transparent)
                        textView.setTextColor(getColor(android.R.attr.textColorPrimary))
                    }
                }
            }
        }

        private fun getColor(attrId: Int): Int {
            val typedValue = TypedValue()
            textView.context.theme.resolveAttribute(attrId, typedValue, true)
            val arr: TypedArray =
                textView.context.obtainStyledAttributes(typedValue.data, intArrayOf(attrId))
            val primaryColor = arr.getColor(0, -1)
            arr.recycle()
            return primaryColor
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val textView =
            inflater.inflate(R.layout.date_picker_calendar_day, parent, false) as TextView
        //TODO: рассмотреть, может базовый layout лучше подойдет
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount() = days.size
}