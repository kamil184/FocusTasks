package com.kamil184.focustasks.ui.dialogs

import android.content.res.Configuration
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.CalendarDay
import com.kamil184.focustasks.data.model.CalendarMonthsHelper
import com.kamil184.focustasks.data.model.OnSelectedDayChangedListener
import com.kamil184.focustasks.data.model.SelectedDay
import com.kamil184.focustasks.util.getColorFromAttr
import java.util.*

class CalendarViewAdapter(
    calendar: Calendar,
    private val selectedDay: SelectedDay,
) :
    RecyclerView.Adapter<CalendarViewAdapter.ViewHolder>() {
    private val days: Array<CalendarDay?> = getDaysOfMonth(calendar)

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
                selectedDay.update(calendarDay.calendar,
                    getDayId(calendarDay.calendar), listener)
            }
            textView.text = calendarDay.getText()
            updateTextColorAndBackground()

            textView.setOnClickListener {
                if (!calendarDay.isSelected) {
                    calendarDay.isSelected = true
                    updateTextColorAndBackground()
                    selectedDay.update(calendarDay.calendar,
                        getDayId(calendarDay.calendar), listener)
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
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.material_dynamic_primary30))
                    else textView.setTextColor(ContextCompat.getColor(textView.context, R.color.material_dynamic_primary70))
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

    /**
     * calculate what day of the week the first day of the month is
     */
    private fun getFirstDayOfWeekOfMonth(calendar: Calendar): Int {
        val copy = calendar.clone() as Calendar
        copy.set(Calendar.DAY_OF_MONTH, 1)
        var firstDayOfWeekOfMonth = copy.get(Calendar.DAY_OF_WEEK) - copy.firstDayOfWeek + 1
        if (firstDayOfWeekOfMonth < 1) firstDayOfWeekOfMonth += 7
        return firstDayOfWeekOfMonth
    }

    fun getDayId(calendar: Calendar) =
        getFirstDayOfWeekOfMonth(calendar) + calendar.get(Calendar.DAY_OF_MONTH) - 2

    //TODO: RTL support
    private fun getDaysOfMonth(c: Calendar): Array<CalendarDay?> {
        val days: Array<CalendarDay?> = Array(42) { null } // 42 = 6*7
        val calendar: Calendar = c.clone() as Calendar

        val firstDayOfWeekOfMonth = getFirstDayOfWeekOfMonth(calendar)

        // fill the array, leaving empty fields that are not included in the current month
        for (i in firstDayOfWeekOfMonth - 1 until calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + firstDayOfWeekOfMonth - 1) {
            days[i] = getDay(calendar)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun getDay(calendar: Calendar): CalendarDay {
        val day = calendar.clone() as Calendar

        val isToday = areDatesEquals(calendar, CalendarMonthsHelper.today)

        val isSelected = areDatesEquals(calendar, selectedDay.calendar)

        return CalendarDay(day, isToday, isSelected)
    }

    private fun areDatesEquals(c1: Calendar, c2: Calendar) =
        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
}