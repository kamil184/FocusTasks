package com.kamil184.focustasks.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.kamil184.focustasks.databinding.DatePickerDialogBinding
import com.kamil184.focustasks.model.CalendarMonthHelper

class DatePickerDialog : DialogFragment() {
    private var _binding: DatePickerDialogBinding? = null
    private val binding get() = _binding!!

    private val onPageChangeCallback = object :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val title = CalendarMonthHelper.getMonthTitle(requireContext(),
                CalendarMonthHelper.allMonths[position])
            binding.datePickerMonthTitle.text = title
        }
    }

    private val adapter = CalendarViewPagerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DatePickerDialogBinding.inflate(inflater, container, false)
        binding.datePickerCalendarPager.offscreenPageLimit = 1
        binding.datePickerCalendarPager.adapter = adapter
        binding.datePickerCalendarPager.registerOnPageChangeCallback(onPageChangeCallback)
        binding.datePickerCalendarPager.setCurrentItem(
            CalendarMonthHelper.getMonthId(CalendarMonthHelper.today), false)
        setDaysHeader()

        return binding.root
    }

    private fun setDaysHeader() {
        val daysHeadersList = CalendarMonthHelper.getDaysHeadersList(requireContext())
        binding.datePickerCalendarHeaderTv1.text = daysHeadersList[0]
        binding.datePickerCalendarHeaderTv2.text = daysHeadersList[1]
        binding.datePickerCalendarHeaderTv3.text = daysHeadersList[2]
        binding.datePickerCalendarHeaderTv4.text = daysHeadersList[3]
        binding.datePickerCalendarHeaderTv5.text = daysHeadersList[4]
        binding.datePickerCalendarHeaderTv6.text = daysHeadersList[5]
        binding.datePickerCalendarHeaderTv7.text = daysHeadersList[6]
    }

    companion object {
        const val TAG = "DatePickerDialog"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.datePickerCalendarPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        _binding = null
    }
}