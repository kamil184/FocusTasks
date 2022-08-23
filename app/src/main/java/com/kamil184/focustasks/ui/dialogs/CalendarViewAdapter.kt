package com.kamil184.focustasks.ui.dialogs

import android.content.res.Configuration
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kamil184.focustasks.R
import com.kamil184.focustasks.model.CalendarDay
import com.kamil184.focustasks.model.CalendarMonthHelper
import com.kamil184.focustasks.model.OnSelectedDayChangedListener
import com.kamil184.focustasks.util.getColorFromAttr

class CalendarViewAdapter(
    private val days: Array<CalendarDay?>,
    private val calendarMonthHelper: CalendarMonthHelper,
) :
    RecyclerView.Adapter<CalendarViewAdapter.ViewHolder>() {

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        private var _calendarDay: CalendarDay? = null
        private val calendarDay get() = _calendarDay!!

        private val listener = object : OnSelectedDayChangedListener {
            override fun onSelectedDayChanged() {
                calendarDay.isSelected = false
                updateTextColorAndBackground()
            }
        }

        fun bind(calendarDay: CalendarDay) {
            _calendarDay = calendarDay

            if (calendarDay.isSelected) {
                calendarMonthHelper.selectedDay.update(calendarDay.calendar,
                    CalendarMonthHelper.getDayId(calendarDay.calendar), listener)
            }
            textView.text = calendarDay.getText()
            updateTextColorAndBackground()

            textView.setOnClickListener {
                if (!calendarDay.isSelected) {
                    calendarDay.isSelected = true
                    updateTextColorAndBackground()
                    calendarMonthHelper.selectedDay.update(calendarDay.calendar,
                        CalendarMonthHelper.getDayId(calendarDay.calendar), listener)
                }
            }
        }

        private fun updateTextColorAndBackground() {
            if (calendarDay.isSelected) {
                textView.setTypeface(null, Typeface.NORMAL)
                textView.setBackgroundResource(R.drawable.date_picker_selected_day_background)
                textView.setTextColor(getColorFromAttr(textView.context, R.attr.colorOnPrimary))
            } else {
                textView.setBackgroundResource(android.R.color.transparent)
                if (calendarDay.isToday) {
                    textView.setTypeface(null, Typeface.BOLD)
                    val currentNightMode = textView.context.resources
                        .configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES)
                        textView.setTextColor(textView.context.getColor(R.color.material_dynamic_primary30))
                    else textView.setTextColor(textView.context.getColor(R.color.material_dynamic_primary70))
                } else {
                    textView.setTypeface(null, Typeface.NORMAL)
                    textView.setTextColor(getColorFromAttr(textView.context, android.R.attr.textColorPrimary))
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val textView =
            inflater.inflate(R.layout.date_picker_calendar_day, parent, false) as TextView
        //TODO: рассмотреть, может базовый layout лучше подойдет (android.R.layout.simple_list_item_1)
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendarDay = days[position]
        if (calendarDay != null) {
            holder.bind(calendarDay)
        }
    }

    override fun getItemCount() = days.size
}