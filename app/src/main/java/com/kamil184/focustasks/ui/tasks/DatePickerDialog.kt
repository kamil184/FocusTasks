package com.kamil184.focustasks.ui.tasks

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.DatePickerDialogBinding
import com.kamil184.focustasks.model.CalendarMonthsHelper
import com.kamil184.focustasks.util.getColorFromAttr
import com.kamil184.focustasks.util.parcelable
import java.util.*

class DatePickerDialog(private val onDismissListener: () -> Unit) : DialogFragment() {
    constructor() : this({})

    private var _binding: DatePickerDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DatePickerViewModel by viewModels()

    private var repeatDialog: RepeatDialog? = null
    private var _timePicker: MaterialTimePicker? = null
    private val timePicker get() = _timePicker!!

    private val onPageChangeCallback = object :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val title = viewModel.getMonthTitle(requireContext(), CalendarMonthsHelper.allMonthsIn21Century[position])
            binding.datePickerMonthTitle.text = title
        }
    }

    private lateinit var adapter: CalendarViewPagerAdapter
    private var isSystem24Hour = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(viewModel.task.value == null)
            viewModel.task.value = arguments?.parcelable(BUNDLE_KEY_TASK)
                ?: throw NullPointerException("DatePickerDialog must get an object of class Task")
        viewModel.taskValue.calendar = viewModel.taskValue.calendar ?: Calendar.getInstance()
        adapter = CalendarViewPagerAdapter(viewModel.taskValue.calendar!!)
        isSystem24Hour = is24HourFormat(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DatePickerDialogBinding.inflate(layoutInflater, null, false)

        restoreViewState()
        binding.datePickerCalendarPager.offscreenPageLimit = 1
        binding.datePickerCalendarPager.adapter = adapter
        binding.datePickerCalendarPager.registerOnPageChangeCallback(onPageChangeCallback)
        binding.datePickerCalendarPager.setCurrentItem(
            viewModel.getIdOfMonthIn21Century(viewModel.taskValue.calendar!!), false)
        setDaysHeader()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton(getString(R.string.dialog_negative_button_text)) { _, _ ->

            }
            .setPositiveButton(getString(R.string.dialog_positive_button_text)) { _, _ ->
                val bundle = Bundle()
                setDateInTask()
                bundle.putParcelable(BUNDLE_KEY_TASK, viewModel.taskValue)
                setFragmentResult(REQUEST_KEY, bundle)
            }
            .setView(binding.root)

        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        binding.datePickerCalendarTimeContainer.setOnClickListener {
            val hour: Int
            val minute:Int
            if(viewModel.taskValue.isThereATime){
                hour = viewModel.taskValue.calendar!!.get(Calendar.HOUR_OF_DAY)
                minute = viewModel.taskValue.calendar!!.get(Calendar.MINUTE)
            }
            else {
                val calendar = Calendar.getInstance()
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
            }
            _timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setTitleText(R.string.select_time)
                .setHour(hour)
                .setMinute(minute)
                .setPositiveButtonText(getString(R.string.dialog_positive_button_text))
                .setNegativeButtonText(getString(R.string.dialog_negative_button_text))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                viewModel.taskValue.isThereATime = true
                viewModel.taskValue.calendar!!.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                viewModel.taskValue.calendar!!.set(Calendar.MINUTE, timePicker.minute)
                val text = viewModel.getTimeText(timePicker.hour, timePicker.minute, isSystem24Hour)
                binding.datePickerCalendarTimeText.text = text
                binding.datePickerCalendarTimeText.setTextColor(getColorFromAttr(requireContext(),
                    android.R.attr.textColorPrimary))
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
            repeatDialog!!.arguments =
                bundleOf(RepeatDialog.BUNDLE_KEY_REPEAT to viewModel.taskValue.repeat)

            repeatDialog!!.show(parentFragmentManager, RepeatDialog.TAG)
            getDialog()?.hide()
        }

        setFragmentResultListener(RepeatDialog.REQUEST_KEY) { key, bundle ->
            viewModel.taskValue.repeat = bundle.parcelable(RepeatDialog.BUNDLE_KEY_REPEAT)
            binding.datePickerCalendarRepeatText.text =
                viewModel.taskValue.repeat?.getText(requireContext())
            binding.datePickerCalendarRepeatText.setTextColor(getColorFromAttr(requireContext(),
                android.R.attr.textColorPrimary))
        }
        return dialog.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        setDateInTask()
    }

    private fun restoreViewState(){
        if(viewModel.taskValue.isThereATime) {
            val text = viewModel.getTimeText(viewModel.taskValue.calendar!!.get(Calendar.HOUR_OF_DAY),
                viewModel.taskValue.calendar!!.get(Calendar.MINUTE), isSystem24Hour)
            binding.datePickerCalendarTimeText.text = text
            binding.datePickerCalendarTimeText.setTextColor(getColorFromAttr(requireContext(),
                android.R.attr.textColorPrimary))
        }
        if(viewModel.taskValue.repeat != null){
            binding.datePickerCalendarRepeatText.text =
                viewModel.taskValue.repeat!!.getText(requireContext())
            binding.datePickerCalendarRepeatText.setTextColor(getColorFromAttr(requireContext(),
                android.R.attr.textColorPrimary))
        }
    }

    private fun setDateInTask() {
        val hours = viewModel.taskValue.calendar!!.get(Calendar.HOUR_OF_DAY)
        val minutes = viewModel.taskValue.calendar!!.get(Calendar.MINUTE)
        val calendar = adapter.selectedDay.calendar
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        viewModel.taskValue.calendar = calendar
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
        val daysHeadersList = viewModel.getDaysHeadersList(requireContext())
        binding.datePickerCalendarHeaderTv1.text = daysHeadersList[0]
        binding.datePickerCalendarHeaderTv2.text = daysHeadersList[1]
        binding.datePickerCalendarHeaderTv3.text = daysHeadersList[2]
        binding.datePickerCalendarHeaderTv4.text = daysHeadersList[3]
        binding.datePickerCalendarHeaderTv5.text = daysHeadersList[4]
        binding.datePickerCalendarHeaderTv6.text = daysHeadersList[5]
        binding.datePickerCalendarHeaderTv7.text = daysHeadersList[6]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.datePickerCalendarPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        _binding = null
    }

    companion object {
        const val TAG = "DatePickerDialog"
        const val REQUEST_KEY = "DatePickerRequestKey"
        const val BUNDLE_KEY_TASK = "BundleKeyTask"
    }
}