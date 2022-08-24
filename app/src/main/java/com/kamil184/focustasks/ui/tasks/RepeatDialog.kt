package com.kamil184.focustasks.ui.tasks

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.RepeatDialogBinding
import java.util.*

class RepeatDialog : DialogFragment(), RepeatDialogDayTextView.StateOnClickListener {
    private var _binding: RepeatDialogBinding? = null
    private val binding get() = _binding!!

    private var _daysList: List<RepeatDialogDayTextView>? = null
    private val daysList get() = _daysList!!

    private val calendar = Calendar.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = RepeatDialogBinding.inflate(layoutInflater, null, false)

        fillDayList()
        setDaysButtonsListeners()
        setDaysWithData()
        binding.repeatDialogEveryTimeUnits.setOnClickListener {
            showTimeUnitsPopup(it)
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

    private fun showTimeUnitsPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.repeat_dialog_every_time_units_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            binding.repeatDialogEveryTimeUnits.text = menuItem.title
            binding.repeatDialogEveryTimeUnits.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                null,
                requireContext().getDrawable(R.drawable.ic_expand_more_24),
                null)

            when (menuItem.title) {
                requireContext().getString(R.string.day) -> hideDaysButtons()
                requireContext().getString(R.string.week) -> showDaysButtons()
                requireContext().getString(R.string.month) -> hideDaysButtons()
                requireContext().getString(R.string.year) -> hideDaysButtons()
            }

            true
        }

        popup.setOnDismissListener {
            binding.repeatDialogEveryTimeUnits.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                null,
                requireContext().getDrawable(R.drawable.ic_expand_more_24),
                null)
        }
        popup.show()

        binding.repeatDialogEveryTimeUnits.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
            null,
            requireContext().getDrawable(R.drawable.ic_expand_less_24),
            null)
    }
    private fun fillDayList() {
        _daysList = listOf(binding.repeatDialogMonday,
            binding.repeatDialogTuesday,
            binding.repeatDialogWednesday,
            binding.repeatDialogThursday,
            binding.repeatDialogFriday,
            binding.repeatDialogSaturday,
            binding.repeatDialogSunday)
    }

    private fun showDaysButtons() =
        daysList.forEach { it.visibility = View.VISIBLE }

    private fun hideDaysButtons() =
        daysList.forEach { it.visibility = View.GONE }

    private fun setDaysButtonsListeners() =
        daysList.forEach { it.stateOnClickListener = this }

    private fun setDaysWithData() {
        val daysStringArray = requireContext().resources.getStringArray(R.array.calendar_days)
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

    override fun isMoreThanOne(): Boolean {
        var count: Byte = 0
        for (day in daysList) {
            if (day.isClicked) count++
        }
        return count > 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _daysList = null
    }
}