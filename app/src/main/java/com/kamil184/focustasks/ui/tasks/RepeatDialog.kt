package com.kamil184.focustasks.ui.tasks

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.RepeatDialogBinding
import com.kamil184.focustasks.util.getColorFromAttr
import java.util.*

class RepeatDialog(private val onCloseListener: () -> Unit) : DialogFragment(),
    RepeatDialogDayTextView.StateOnClickListener {
    private var _binding: RepeatDialogBinding? = null
    private val binding get() = _binding!!
    private val days2LettersArray:Array<CharSequence?> = Array(7){null}

    private var _daysList: List<RepeatDialogDayTextView>? = null
    private val daysList get() = _daysList!!

    private val calendar = Calendar.getInstance()

    private var expandMoreDrawable: Drawable? = null
    private var expandLessDrawable: Drawable? = null

    private val firstRadioBtnOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.repeatDialogSecondRadioBtn.setOnCheckedChangeListener(null)
                binding.repeatDialogSecondRadioBtn.isChecked = false
                binding.repeatDialogSecondRadioBtn.setOnCheckedChangeListener(
                    secondRadioBtnOnCheckedChangeListener)

                binding.repeatDialogFirstRadioText.setTextColor(getColorFromAttr(requireContext(),
                    R.attr.colorOnSurfaceVariant))
                binding.repeatDialogFirstRadioText.isEnabled = true
                binding.repeatDialogFirstRadioText.isClickable = true

                binding.repeatDialogSecondRadioCountText.setTextColor(getColorFromAttr(
                    requireContext(),
                    android.R.attr.textColorHint))
                binding.repeatDialogSecondRadioCountText.isEnabled = false
                binding.repeatDialogSecondRadioCountText.isClickable = false
                binding.repeatDialogSecondRadioDayText.setTextColor(getColorFromAttr(requireContext(),
                    android.R.attr.textColorHint))
                binding.repeatDialogSecondRadioDayText.isEnabled = false
                binding.repeatDialogSecondRadioDayText.isClickable = false
            }
        }

    private val secondRadioBtnOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.repeatDialogFirstRadioBtn.setOnCheckedChangeListener(null)
                binding.repeatDialogFirstRadioBtn.isChecked = false
                binding.repeatDialogFirstRadioBtn.setOnCheckedChangeListener(
                    firstRadioBtnOnCheckedChangeListener)

                binding.repeatDialogSecondRadioCountText.setTextColor(getColorFromAttr(
                    requireContext(),
                    R.attr.colorOnSurfaceVariant))
                binding.repeatDialogSecondRadioCountText.isEnabled = true
                binding.repeatDialogSecondRadioCountText.isClickable = true
                binding.repeatDialogSecondRadioDayText.setTextColor(getColorFromAttr(requireContext(),
                    R.attr.colorOnSurfaceVariant))
                binding.repeatDialogSecondRadioDayText.isEnabled = true
                binding.repeatDialogSecondRadioDayText.isClickable = true

                binding.repeatDialogFirstRadioText.setTextColor(getColorFromAttr(requireContext(),
                    android.R.attr.textColorHint))
                binding.repeatDialogFirstRadioText.isEnabled = false
                binding.repeatDialogFirstRadioText.isClickable = false
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = RepeatDialogBinding.inflate(layoutInflater, null, false)

        expandMoreDrawable =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_expand_more_24)
        expandLessDrawable =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_expand_less_24)

        fillDayList()
        fillDays2LettersList()

        binding.repeatDialogFirstRadioBtn.setOnCheckedChangeListener(
            firstRadioBtnOnCheckedChangeListener)
        binding.repeatDialogSecondRadioBtn.setOnCheckedChangeListener(
            secondRadioBtnOnCheckedChangeListener)

        // if there is no data
        binding.repeatDialogRepeatsTimeUnitsText.setText(R.string.week)
        binding.repeatDialogFirstRadioBtn.isChecked = true
        binding.repeatDialogFirstRadioText.setText(R.string.day_1)
        binding.repeatDialogSecondRadioCountText.setText(R.string.first)
        binding.repeatDialogSecondRadioDayText.text = days2LettersArray[0]

        setDaysButtonsListeners()
        setDaysListWithCalendar()

        showUpDependingOnTimeUnits(binding.repeatDialogRepeatsTimeUnitsText.text)


        binding.repeatDialogRepeatsTimeUnitsText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogRepeatsTimeUnitsText,
                R.menu.repeat_dialog_repeats_time_units_menu) {
                showUpDependingOnTimeUnits(binding.repeatDialogRepeatsTimeUnitsText.text)
            }
        }

        binding.repeatDialogFirstRadioText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogFirstRadioText,
                R.menu.repeat_dialog_first_radio_menu, null)
        }

        binding.repeatDialogSecondRadioCountText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogSecondRadioCountText,
                R.menu.repeat_dialog_second_radio_count_menu, null)
        }

        binding.repeatDialogSecondRadioDayText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogSecondRadioDayText,
                days2LettersArray, null)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton("Закрыть") { _, _ ->

            }
            .setPositiveButton("Готово") { _, _ ->
                //TODO save result
            }
            .setView(binding.root)

        return dialog.create()
    }

    companion object {
        const val TAG = "RepeatDialog"
    }

    private fun showPopupWithExpandDrawables(
        textView: TextView,
        @MenuRes menuRes: Int,
        onMenuItemClick: (() -> Unit)?,
    ) {
        val popup = PopupMenu(requireContext(), textView)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            textView.text = menuItem.title
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                expandMoreDrawable,
                null)
            onMenuItemClick?.invoke()
            true
        }

        popup.setOnDismissListener {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                expandMoreDrawable,
                null)
        }
        popup.show()

        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            expandLessDrawable,
            null)
    }

    private fun showPopupWithExpandDrawables(
        textView: TextView,
        menuItems: Array<CharSequence?>,
        onMenuItemClick: (() -> Unit)?,
    ) {
        val popup = PopupMenu(requireContext(), textView)
        for (item in menuItems)
            popup.menu.add(item)

        popup.setOnMenuItemClickListener { menuItem ->
            textView.text = menuItem.title
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                expandMoreDrawable,
                null)
            onMenuItemClick?.invoke()
            true
        }

        popup.setOnDismissListener {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                expandMoreDrawable,
                null)
        }
        popup.show()

        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            expandLessDrawable,
            null)
    }

    private fun showUpDependingOnTimeUnits(timeUnit: CharSequence) {
        when (timeUnit) {
            requireContext().getString(R.string.day) -> {
                hideDaysButtons()
                hideMonthsRadioButtons()
            }
            requireContext().getString(R.string.week) -> {
                showDaysButtons()
                hideMonthsRadioButtons()
            }
            requireContext().getString(R.string.month) -> {
                hideDaysButtons()
                showMonthsRadioButtons()
            }
            requireContext().getString(R.string.year) -> {
                hideDaysButtons()
                hideMonthsRadioButtons()
            }
        }
    }

    private fun fillDayList() {
        _daysList = listOf(binding.repeatDialogDay1,
            binding.repeatDialogDay2,
            binding.repeatDialogDay3,
            binding.repeatDialogDay4,
            binding.repeatDialogDay5,
            binding.repeatDialogDay6,
            binding.repeatDialogDay7)
    }

    private fun showDaysButtons() =
        daysList.forEach { it.visibility = View.VISIBLE }

    private fun hideDaysButtons() =
        daysList.forEach { it.visibility = View.GONE }

    private fun setDaysButtonsListeners() =
        daysList.forEach { it.stateOnClickListener = this }

    private fun setDaysListWithCalendar() {
        val daysStringArray = requireContext().resources.getStringArray(R.array.calendar_days_1_letter)
        val diff = calendar.firstDayOfWeek - 1
        val ti = calendar.get(Calendar.DAY_OF_WEEK) - 1 - diff
        val todayId =
            if (ti < 0) ti + 7
            else ti

        for (i in daysList.indices) {
            if (i + diff > 6)
                daysList[i].text = daysStringArray[i + diff - 7]
            else
                daysList[i].text = daysStringArray[i + diff]

            if (i == todayId)
                daysList[i].setClickedState()
            else daysList[i].setUnClickedState()
        }
    }

    private fun fillDays2LettersList(){
        val daysStringArray = requireContext().resources.getStringArray(R.array.calendar_days_2_letters)
        val diff = calendar.firstDayOfWeek - 1
        for (i in daysStringArray.indices) {
            if (i + diff > 6)
                days2LettersArray[i] = daysStringArray[i + diff - 7]
            else
                days2LettersArray[i] = daysStringArray[i + diff]
        }
    }
    override fun isMoreThanOne(): Boolean {
        var count: Byte = 0
        for (day in daysList) {
            if (day.isClicked) count++
        }
        return count > 1
    }

    private fun hideMonthsRadioButtons() {
        binding.repeatDialogFirstRadioBtn.visibility = View.GONE
        binding.repeatDialogFirstRadioText.visibility = View.GONE
        binding.repeatDialogSecondRadioBtn.visibility = View.GONE
        binding.repeatDialogSecondRadioDayText.visibility = View.GONE
        binding.repeatDialogSecondRadioCountText.visibility = View.GONE
    }

    private fun showMonthsRadioButtons() {
        binding.repeatDialogFirstRadioBtn.visibility = View.VISIBLE
        binding.repeatDialogFirstRadioText.visibility = View.VISIBLE
        binding.repeatDialogSecondRadioBtn.visibility = View.VISIBLE
        binding.repeatDialogSecondRadioDayText.visibility = View.VISIBLE
        binding.repeatDialogSecondRadioCountText.visibility = View.VISIBLE
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCloseListener.invoke()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onCloseListener.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _daysList = null
    }
}