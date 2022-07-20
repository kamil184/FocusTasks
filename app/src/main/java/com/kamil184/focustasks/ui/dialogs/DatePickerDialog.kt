package com.kamil184.focustasks.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.DatePickerDialogBinding
import com.kamil184.focustasks.model.CalendarMonthHelper

class DatePickerDialog(private val onDismissListener: () -> Unit) : DialogFragment() {
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


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DatePickerDialogBinding.inflate(layoutInflater, null, false)
        binding.datePickerCalendarPager.offscreenPageLimit = 1
        binding.datePickerCalendarPager.adapter = adapter
        binding.datePickerCalendarPager.registerOnPageChangeCallback(onPageChangeCallback)
        binding.datePickerCalendarPager.setCurrentItem(
            CalendarMonthHelper.getMonthId(CalendarMonthHelper.today), false)
        setDaysHeader()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton("Закрыть"){ _, _ ->

            }
            .setPositiveButton("Готово"){ _, _ ->

            }
            .setView(binding.root)
        return dialog.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener.invoke()
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