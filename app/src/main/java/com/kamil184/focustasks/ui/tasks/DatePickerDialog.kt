package com.kamil184.focustasks.ui.tasks

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.DatePickerDialogBinding
import com.kamil184.focustasks.model.CalendarMonthHelper
import com.kamil184.focustasks.ui.calendar.CalendarViewModel
import java.util.*


class DatePickerDialog(private val onDismissListener: () -> Unit) : DialogFragment() {
    private var _binding: DatePickerDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DatePickerViewModel

    private var repeatDialog: RepeatDialog? = null
    private var _timePicker: MaterialTimePicker? = null
    private val timePicker get() = _timePicker!!

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
        viewModel =
            ViewModelProvider(this).get(DatePickerViewModel::class.java)

        val isSystem24Hour = is24HourFormat(context)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        binding.datePickerCalendarPager.offscreenPageLimit = 1
        binding.datePickerCalendarPager.adapter = adapter
        binding.datePickerCalendarPager.registerOnPageChangeCallback(onPageChangeCallback)
        binding.datePickerCalendarPager.setCurrentItem(
            CalendarMonthHelper.getMonthId(CalendarMonthHelper.today), false)
        setDaysHeader()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton(getString(R.string.dialog_negative_button_text)) { _, _ ->

            }
            .setPositiveButton(getString(R.string.dialog_positive_button_text)) { _, _ ->
                val bundle = Bundle()
                bundle.putParcelable(BUNDLE_KEY_REPEAT, viewModel.repeat.value)
                bundle.putSerializable(BUNDLE_KEY_CALENDAR, adapter.calendarMonthHelper.selectedDay.calendar)
                setFragmentResult(REQUEST_KEY, bundle)
            }
            .setView(binding.root)

        binding.datePickerCalendarTimeContainer.setOnClickListener {
            val calendar = Calendar.getInstance()
            _timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setTitleText(R.string.select_time)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .setPositiveButtonText(getString(R.string.dialog_positive_button_text))
                .setNegativeButtonText(getString(R.string.dialog_negative_button_text))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val text = viewModel.getTimeText(timePicker.hour, timePicker.minute, isSystem24Hour)
                binding.datePickerCalendarTimeText.text = text
            }

            timePicker.addOnCancelListener {
                getDialog()?.show()
                _timePicker = null
            }

            timePicker.addOnDismissListener {
                getDialog()?.show()
                _timePicker = null
            }
            timePicker.show(parentFragmentManager, "MaterialTimePicker")
            getDialog()?.hide()
        }

        binding.datePickerCalendarRepeatContainer.setOnClickListener {
            repeatDialog = RepeatDialog {
                getDialog()?.show()
                repeatDialog = null
            }
            repeatDialog?.show(parentFragmentManager, RepeatDialog.TAG)
            getDialog()?.hide()

        }

        setFragmentResultListener(RepeatDialog.REQUEST_KEY) { key, bundle ->
            Log.d("FragmentResult", "childFragmentManager listener")
            viewModel.repeat.value = bundle.getParcelable(RepeatDialog.BUNDLE_KEY_REPEAT)
            binding.datePickerCalendarRepeatText.text = viewModel.repeat.value?.getText(requireContext())
        }
        return dialog.create()
    }

    override fun onResume() {
        super.onResume()
        if (repeatDialog != null) dialog?.hide()
        if (_timePicker != null) dialog?.hide()
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
        const val REQUEST_KEY = "DatePickerRequestKey"
        const val BUNDLE_KEY_REPEAT = "BundleKeyRepeat"
        const val BUNDLE_KEY_CALENDAR = "BundleKeyCalendar"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.datePickerCalendarPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        _binding = null
    }
}