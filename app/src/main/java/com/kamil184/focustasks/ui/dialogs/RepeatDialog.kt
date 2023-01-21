package com.kamil184.focustasks.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.CalendarMonthsHelper.Companion.today
import com.kamil184.focustasks.data.model.Repeat
import com.kamil184.focustasks.databinding.RepeatDialogBinding
import com.kamil184.focustasks.ui.dialogs.RepeatDialogViewModel.Companion.localeDays2LettersArray
import com.kamil184.focustasks.util.*
import java.util.*

class RepeatDialog(
    private val onCloseListener: () -> Unit,
) : DialogFragment(),
    RepeatDialogDayTextView.StateOnClickListener {
    constructor() : this({})

    private val calendarDaysFrom1to31: Array<String> by lazy { resources.getStringArray(R.array.calendar_days_from_1_to_31) }
    private val repeatTimeUnits: Array<String> by lazy {
        arrayOf(getString(R.string.day),
            getString(R.string.week),
            getString(R.string.month),
            getString(R.string.year))
    }
    private val secondRadioCountArray: Array<String> by lazy {
        arrayOf(getString(R.string.first),
            getString(R.string.second),
            getString(R.string.third),
            getString(R.string.fourth),
            getString(R.string.last))
    }

    private var _binding: RepeatDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RepeatDialogViewModel by viewModels()

    private val daysViewsList: List<RepeatDialogDayTextView> by lazy {
        listOf(binding.repeatDialogDay1,
            binding.repeatDialogDay2,
            binding.repeatDialogDay3,
            binding.repeatDialogDay4,
            binding.repeatDialogDay5,
            binding.repeatDialogDay6,
            binding.repeatDialogDay7)
    }

    private val daysListChecked: BooleanArray
        get() {
            val result = BooleanArray(7)
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

        //TODO: генерировать данные исходя из выбранного дня в DatePickerDialog
        //      (например, если выбрано 23 февраля, то при выборе "каждый .. месяц" будет 23 число)
        if (viewModel.repeat.value == null)
            viewModel.repeat.value =
                arguments?.parcelable(BUNDLE_KEY_REPEAT) ?: Repeat.getBasicInstance()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = RepeatDialogBinding.inflate(layoutInflater, null, false)

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
                repeatTimeUnits) {
                showUpDependingOnTimeUnits(binding.repeatDialogRepeatsTimeUnitsText.text)
            }
        }

        binding.repeatDialogFirstRadioText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogFirstRadioText,
                calendarDaysFrom1to31, null)
        }

        binding.repeatDialogSecondRadioCountText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogSecondRadioCountText,
                secondRadioCountArray, null)
        }

        binding.repeatDialogSecondRadioDayText.setOnClickListener {
            showPopupWithExpandDrawables(binding.repeatDialogSecondRadioDayText,
                localeDays2LettersArray, null)
        }

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setNegativeButton(getString(R.string.dialog_negative_button_text), null)
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
        viewModel.repeat.value = buildRepeat()
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
                repeat.weekInfo = daysListChecked

            requireContext().getString(R.string.month) -> {
                if (binding.repeatDialogFirstRadioBtn.isChecked) {
                    var isInfoInitialized = false
                    for (i in 1..31)
                        if (binding.repeatDialogFirstRadioText.text.contains(i.toString())) {
                            repeat.monthInfoInt = i
                            isInfoInitialized = true
                        }
                    if (!isInfoInitialized) repeat.monthInfoInt = 32
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
                    for (i in localeDays2LettersArray.indices)
                        if (binding.repeatDialogSecondRadioDayText.text.equals(
                                localeDays2LettersArray[i])
                        ) {
                            secondInPair = convertToNormalDay(i)
                            break
                        }
                    if (secondInPair == -1) throw IllegalArgumentException("variable secondInPair must be in 0..6")

                    repeat.monthInfoPair = Pair(firstInPair, secondInPair)
                }
            }

        }
    }

    private fun restoreViewState() {
        viewModel.repeat.value?.apply {
            binding.repeatDialogRepeatsCountEditText.setText(count.toString())
            binding.repeatDialogRepeatsTimeUnitsText.setText(getNameRes())
            setDaysList()
            when {
                monthInfoPair != null -> { //MONTH
                    val pairInfo = monthInfoPair!!
                    binding.repeatDialogSecondRadioBtn.isChecked = true

                    val countTextRes = when (pairInfo.first) {
                        1 -> R.string.first
                        2 -> R.string.second
                        3 -> R.string.third
                        4 -> R.string.fourth
                        5 -> R.string.last
                        else -> throw IllegalArgumentException("first variable in info must be in 1..5")
                    }

                    binding.repeatDialogSecondRadioCountText.setText(countTextRes)

                    val id = convertToLocalDay(pairInfo.second)
                    binding.repeatDialogSecondRadioDayText.text = localeDays2LettersArray[id]

                    if (binding.repeatDialogFirstRadioText.text.isEmpty())
                        binding.repeatDialogFirstRadioText.text =
                            calendarDaysFrom1to31[today.get(Calendar.DAY_OF_MONTH) - 1]
                    return@apply
                }
                monthInfoInt != null -> { //MONTH
                    val intInfo = monthInfoInt!!
                    binding.repeatDialogFirstRadioBtn.isChecked = true
                    binding.repeatDialogFirstRadioText.text =
                        calendarDaysFrom1to31[intInfo - 1]
                    if (binding.repeatDialogSecondRadioCountText.text.isEmpty()) {
                        binding.repeatDialogSecondRadioCountText.setText(Repeat.getNumberOfTheWeekInMonthStringRes())
                        binding.repeatDialogSecondRadioDayText.text =
                            localeDays2LettersArray[getTodayLocalIndex()]
                    }
                    return@apply
                }
                weekInfo != null -> { //WEEK
                    if (binding.repeatDialogFirstRadioText.text.isEmpty()) {
                        binding.repeatDialogFirstRadioBtn.isChecked = true
                        binding.repeatDialogFirstRadioText.text =
                            calendarDaysFrom1to31[today.get(Calendar.DAY_OF_MONTH) - 1]
                        binding.repeatDialogSecondRadioCountText.setText(Repeat.getNumberOfTheWeekInMonthStringRes())
                        binding.repeatDialogSecondRadioDayText.text =
                            localeDays2LettersArray[getTodayLocalIndex()]
                    }
                    return@apply
                }
            }
        }
    }

    private fun setDaysList() {
        if (viewModel.repeat.value?.weekInfo != null) {
            val isDaysClickedArray =
                viewModel.repeat.value?.weekInfo!!

            for (i in daysViewsList.indices) {
                if (isDaysClickedArray[i]) daysViewsList[i].setClickedState()
                else daysViewsList[i].setUnClickedState()
            }
        } else {
            val todayId = convertToLocalDay(today.get(Calendar.DAY_OF_WEEK) - 1)

            for (i in daysViewsList.indices) {
                if (i == todayId) daysViewsList[i].setClickedState()
                else daysViewsList[i].setUnClickedState()
            }
        }
    }

    companion object {
        const val TAG = "RepeatDialog"
        const val REQUEST_KEY = "RepeatRequestKey"
        const val BUNDLE_KEY_REPEAT = "BundleKeyRepeat"
    }

    private fun showPopupWithExpandDrawables(
        textView: TextView,
        menuItems: Array<String>,
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

    private fun showDaysViews() =
        daysViewsList.forEach { it.visibility = View.VISIBLE }

    private fun hideDaysViews() =
        daysViewsList.forEach { it.visibility = View.GONE }

    private fun setDaysViewsListeners() =
        daysViewsList.forEach { it.stateOnClickListener = this }

    private fun fillDaysViewsTexts() {
        val daysStringArray =
            requireContext().resources.getStringArray(R.array.calendar_days_1_letter)
        for (i in daysViewsList.indices)
            daysViewsList[i].text = daysStringArray[convertToNormalDay(i)]
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
    }
}