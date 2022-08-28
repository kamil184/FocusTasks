package com.kamil184.focustasks.ui.tasks

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kamil184.focustasks.databinding.DatePickerDialogBinding
import com.kamil184.focustasks.model.CalendarMonthHelper
import java.util.*


class DatePickerDialog(private val onDismissListener: () -> Unit) : DialogFragment() {
    private var _binding: DatePickerDialogBinding? = null
    private val binding get() = _binding!!

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
        binding.datePickerCalendarPager.offscreenPageLimit = 1
        binding.datePickerCalendarPager.adapter = adapter
        binding.datePickerCalendarPager.registerOnPageChangeCallback(onPageChangeCallback)
        binding.datePickerCalendarPager.setCurrentItem(
            CalendarMonthHelper.getMonthId(CalendarMonthHelper.today), false)
        setDaysHeader()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton("Закрыть") { _, _ ->

            }
            .setPositiveButton("Готово") { _, _ ->
                //TODO save result
            }
            .setView(binding.root)

        binding.datePickerCalendarTimeContainer.setOnClickListener {
            val isSystem24Hour = is24HourFormat(context)
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val calendar = Calendar.getInstance()
            _timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val text = if (isSystem24Hour) {
                    "${timePicker.hour}:${timePicker.minute}"
                } else {
                    val isAm = timePicker.hour < 12
                    var h = if (isAm) timePicker.hour
                    else timePicker.hour - 12
                    if (h == 0) h = 12
                    val amPmText = if (isAm) "AM" else "PM"
                    val m =
                        if (timePicker.minute < 10) "0${timePicker.minute}" else "${timePicker.minute}"
                    "$amPmText $h:$m"
                }
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
            repeatDialog = RepeatDialog({ repeat ->
                binding.datePickerCalendarRepeatText.text = repeat.getText(requireContext())
            }, {
                getDialog()?.show()
                repeatDialog = null
            })
            repeatDialog?.show(parentFragmentManager, RepeatDialog.TAG)
            getDialog()?.hide()

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.datePickerCalendarPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        _binding = null
    }
}