package com.kamil184.focustasks.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kamil184.focustasks.databinding.DatePickerDialogBinding
import com.kamil184.focustasks.model.CalendarMonthHelper


class DatePickerDialog : DialogFragment() {
    private var _binding: DatePickerDialogBinding? = null
    private val binding get() = _binding!!

    private val calendarMonthHelper = CalendarMonthHelper()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DatePickerDialogBinding.inflate(inflater, container, false)
        binding.datePickerCalendarView.layoutManager = GridLayoutManager(context, 7)
        (binding.datePickerCalendarView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.datePickerCalendarView.adapter = CalendarViewAdapter(calendarMonthHelper)

        return binding.root
    }


    companion object {
        const val TAG = "DatePickerDialog"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}