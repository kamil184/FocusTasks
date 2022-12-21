package com.kamil184.focustasks.ui.tasks

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.RepeatDialogBinding
import com.kamil184.focustasks.model.CalendarMonthHelper.Companion.localeDays2LettersArray
import com.kamil184.focustasks.model.CalendarMonthHelper.Companion.today
import com.kamil184.focustasks.model.Repeat
import com.kamil184.focustasks.util.getColorFromAttr
import com.kamil184.focustasks.util.parcelable
import java.util.*

class RepeatDialog(
    private val onCloseListener: () -> Unit,
) : DialogFragment(),
    RepeatDialogDayTextView.StateOnClickListener {
    constructor():this({})

    private var _binding: RepeatDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RepeatViewModel by viewModels()

    private var _daysViewsList: List<RepeatDialogDayTextView>? = null
    private val daysViewsList get() = _daysViewsList!!
    private val daysListChecked: Array<Boolean>
        get() {
            val result = Array(7) { false }
            for (dayId in daysViewsList.indices) {
                result[dayId] = daysViewsList[dayId].isClicked
            }
            return result
        }

    private val expandMoreDrawable by lazy {
        AppCompatResources.getDrawable(requireContext(),
            R.drawable.ic_expand_more_24)
    }
    private val expandLessDrawable by lazy {
        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_expand_less_24)
    }

    private val firstRadioBtnOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
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
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fillLocaleDays2LettersList(requireContext())
        viewModel.fillFirstRadioBtnMenuItems(requireContext())

        //TODO: генерировать данные исходя из выбранного дня в DatePickerDialog
        //      (например, если выбрано 23 февраля, то при выборе "каждый .. месяц" будет 23 число)
        viewModel.repeat.value = savedInstanceState?.parcelable(BUNDLE_KEY_REPEAT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = RepeatDialogBinding.inflate(layoutInflater, null, false)

        fillDaysViewsList()

        binding.repeatDialogFirstRadioBtn.setOnCheckedChangeListener(
            firstRadioBtnOnCheckedChangeListener)
        binding.repeatDialogSecondRadioBtn.setOnCheckedChangeListener(
            secondRadioBtnOnCheckedChangeListener)

        restoreViewState()
        setDaysViewsListeners()
        fillDaysViewsTexts()

        showUpDependingOnTimeUnits(binding.repeatDialogRepeatsTimeUnitsText.text)

        binding.repeatDialogRepeatsTimeUnitsText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogRepeatsTimeUnitsText,
                R.menu.repeat_dialog_repeats_time_units_menu) {
                showUpDependingOnTimeUnits(binding.repeatDialogRepeatsTimeUnitsText.text)
            }
        }

        binding.repeatDialogFirstRadioText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogFirstRadioText,
                viewModel.firstRadioBtnMenuItems, null)
        }

        binding.repeatDialogSecondRadioCountText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogSecondRadioCountText,
                R.menu.repeat_dialog_second_radio_count_menu, null)
        }

        binding.repeatDialogSecondRadioDayText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogSecondRadioDayText,
                localeDays2LettersArray, null)
        }

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton(getString(R.string.dialog_negative_button_text)) { _, _ ->

            }
            .setPositiveButton(getString(R.string.dialog_positive_button_text)) { _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_REPEAT to buildRepeat()))
            } //TODO: text
            .setView(binding.root)

        val dialog = dialogBuilder.create()

        binding.repeatDialogRepeatsCountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var isEnabled = false
                try {
                    if (s != null || s.toString().isNotEmpty())
                        isEnabled = s.toString().toInt() >= 1
                } catch (ignore: NumberFormatException) {
                }

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                    isEnabled

            }

            override fun afterTextChanged(s: Editable?) {}

        })

        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_KEY_REPEAT, viewModel.repeat.value)
    }

    private fun buildRepeat(): Repeat {
        val repeat = when (binding.repeatDialogRepeatsTimeUnitsText.text) {
            requireContext().getString(R.string.day) -> Repeat.DAY
            requireContext().getString(R.string.week) -> Repeat.WEEK
            requireContext().getString(R.string.month) -> Repeat.MONTH
            requireContext().getString(R.string.year) -> Repeat.YEAR
            else -> throw IllegalArgumentException("Repeat text must be day, week, month or year")
        }

        setRepeatInfo(repeat)

        repeat.count = binding.repeatDialogRepeatsCountEditText.text.toString().toInt()
        return repeat
    }

    private fun setRepeatInfo(repeat: Repeat) {
        when (binding.repeatDialogRepeatsTimeUnitsText.text) {
            requireContext().getString(R.string.week) ->
                repeat.info = daysListChecked

            requireContext().getString(R.string.month) -> {
                if (binding.repeatDialogFirstRadioBtn.isChecked) {
                    var isInfoInitialized = false
                    for (i in 1..31)
                        if (binding.repeatDialogFirstRadioText.text.contains(i.toString())) {
                            repeat.info = i
                            isInfoInitialized = true
                        }
                    if (!isInfoInitialized) repeat.info = 32
                } else {
                    val firstInPair = when (binding.repeatDialogSecondRadioCountText.text) {
                        requireContext().getString(R.string.first) -> 1
                        requireContext().getString(R.string.second) -> 2
                        requireContext().getString(R.string.third) -> 3
                        requireContext().getString(R.string.fourth) -> 4
                        requireContext().getString(R.string.last) -> 5
                        else -> throw IllegalArgumentException("variable firstInPair must be in 1..5")
                    }
                    var secondInPair = -1
                    val diff = today.firstDayOfWeek - 1
                    for (i in localeDays2LettersArray.indices)
                        if (binding.repeatDialogSecondRadioDayText.text.equals(
                                localeDays2LettersArray[i])
                        )
                            secondInPair = i + 1 + diff
                    if (secondInPair > 7) secondInPair -= 7
                    if (secondInPair !in 1..7) throw IllegalArgumentException("variable secondInPair must be in 1..7")

                    repeat.info = Pair(firstInPair, secondInPair)
                }
            }

        }
    }

    private fun restoreViewState() {
        if (viewModel.repeat.value != null) {
            binding.repeatDialogRepeatsCountEditText.setText(viewModel.repeat.value?.count.toString())
            binding.repeatDialogRepeatsTimeUnitsText.setText(viewModel.repeat.value!!.getNameRes())
            when (viewModel.repeat.value?.info) {
                is Pair<*, *> -> {
                    val pairInfo = viewModel.repeat.value?.info as Pair<*, *>

                    binding.repeatDialogSecondRadioBtn.isChecked = true
                    binding.repeatDialogFirstRadioText.setText(R.string.day_1)

                    val countTextRes = when (pairInfo.first as Int) {
                        1 -> R.string.first
                        2 -> R.string.second
                        3 -> R.string.third
                        4 -> R.string.fourth
                        5 -> R.string.last
                        else -> throw IllegalArgumentException("first variable in info must be in 1..5")
                    }

                    binding.repeatDialogSecondRadioCountText.setText(countTextRes)
                    val diff = today.firstDayOfWeek - 1
                    var id = pairInfo.second as Int - 1 - diff
                    if (id < 0) id += 7
                    binding.repeatDialogSecondRadioDayText.text = localeDays2LettersArray[id]
                    setDaysListCheckedWithCalendar()
                }
                is Int -> {
                    val intInfo = viewModel.repeat.value?.info as Int
                    binding.repeatDialogFirstRadioBtn.isChecked = true
                    binding.repeatDialogFirstRadioText.text =
                        viewModel.firstRadioBtnMenuItems[intInfo - 1]
                    binding.repeatDialogSecondRadioCountText.setText(R.string.first)
                    binding.repeatDialogSecondRadioDayText.text = localeDays2LettersArray[0]
                    setDaysListCheckedWithCalendar()
                }
                is Array<*> -> {
                    val isDaysClickedArray =
                        (viewModel.repeat.value?.info as Array<*>).filterIsInstance<Boolean>()

                    for (i in daysViewsList.indices) {
                        if (isDaysClickedArray[i]) daysViewsList[i].setClickedState()
                        else daysViewsList[i].setUnClickedState()
                    }
                    binding.repeatDialogFirstRadioBtn.isChecked = true
                    binding.repeatDialogFirstRadioText.setText(R.string.day_1)
                    binding.repeatDialogSecondRadioCountText.setText(R.string.first)
                    binding.repeatDialogSecondRadioDayText.text = localeDays2LettersArray[0]
                }
            }
        } else {
            binding.repeatDialogRepeatsCountEditText.setText("1")
            binding.repeatDialogRepeatsTimeUnitsText.setText(R.string.week)
            binding.repeatDialogFirstRadioBtn.isChecked = true
            binding.repeatDialogFirstRadioText.setText(R.string.day_1)
            binding.repeatDialogSecondRadioCountText.setText(R.string.first)
            binding.repeatDialogSecondRadioDayText.text = localeDays2LettersArray[0]

            setDaysListCheckedWithCalendar()
        }
    }

    private fun setDaysListCheckedWithCalendar() {
        val diff = today.firstDayOfWeek - 1
        val ti = today.get(Calendar.DAY_OF_WEEK) - 1 - diff
        val todayId =
            if (ti < 0) ti + 7
            else ti

        for (i in daysViewsList.indices) {
            if (i == todayId) daysViewsList[i].setClickedState()
            else daysViewsList[i].setUnClickedState()
        }
    }

    companion object {
        const val TAG = "RepeatDialog"
        const val REQUEST_KEY = "RepeatRequestKey"
        const val BUNDLE_KEY_REPEAT = "BundleKeyRepeat"
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
        menuItems: Array<CharSequence>,
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
                hideDaysViews()
                hideMonthsRadioButtons()
            }
            requireContext().getString(R.string.week) -> {
                showDaysViews()
                hideMonthsRadioButtons()
            }
            requireContext().getString(R.string.month) -> {
                hideDaysViews()
                showMonthsRadioButtons()
            }
            requireContext().getString(R.string.year) -> {
                hideDaysViews()
                hideMonthsRadioButtons()
            }
        }
    }

    private fun fillDaysViewsList() {
        _daysViewsList = listOf(binding.repeatDialogDay1,
            binding.repeatDialogDay2,
            binding.repeatDialogDay3,
            binding.repeatDialogDay4,
            binding.repeatDialogDay5,
            binding.repeatDialogDay6,
            binding.repeatDialogDay7)
    }

    private fun showDaysViews() =
        daysViewsList.forEach { it.visibility = View.VISIBLE }

    private fun hideDaysViews() =
        daysViewsList.forEach { it.visibility = View.GONE }

    private fun setDaysViewsListeners() =
        daysViewsList.forEach { it.stateOnClickListener = this }

    private fun fillDaysViewsTexts() {
        val daysStringArray =
            requireContext().resources.getStringArray(R.array.calendar_days_1_letter)
        val diff = today.firstDayOfWeek - 1

        for (i in daysViewsList.indices) {
            if (i + diff > 6)
                daysViewsList[i].text = daysStringArray[i + diff - 7]
            else
                daysViewsList[i].text = daysStringArray[i + diff]
        }
    }

    override fun isMoreThanOne(): Boolean {
        var count: Byte = 0
        for (day in daysViewsList) {
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
        _daysViewsList = null
    }
}