package com.kamil184.focustasks.ui.tasks

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.TaskCreateBinding
import com.kamil184.focustasks.model.Task
import com.kamil184.focustasks.util.parcelable


class TaskCreateBottomSheet : BottomSheetDialogFragment() {
    private var _binding: TaskCreateBinding? = null
    private val binding get() = _binding!!
    private var datePickerDialog: DatePickerDialog? = null

    private val viewModel: TaskCreateViewModel by viewModels()

    private var _iconMarginPx: Int? = null
    private val iconMarginPx: Int get() = _iconMarginPx!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.task.value == null)
            viewModel.task.value = arguments?.parcelable(BUNDLE_KEY_TASK) ?: Task()
        viewModel.taskListNames = arguments?.getStringArrayList(BUNDLE_KEY_LIST)

        _iconMarginPx =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                0F, context?.resources?.displayMetrics).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = TaskCreateBinding.inflate(inflater, container, false)

        restoreViewState()

        updateCalendarChip()

        binding.taskCreateCalendarChip.setOnCloseIconClickListener {
            it.visibility = View.GONE
            viewModel.task.value?.apply {
                calendar = null
                isThereATime = false
                repeat = null
            }
        }

        binding.taskCreateCalendarChip.setOnClickListener {
            showDatePickerDialog()
        }

        binding.taskCreateCalendarButton.setOnClickListener {
            showDatePickerDialog()
        }

        binding.taskCreatePriorityButton.setOnClickListener {
            showPriorityPopup(it)
        }

        binding.taskCreateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                viewModel.task.value?.title = p0.toString()
            }
        })

        binding.taskCreateDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                viewModel.task.value?.description = p0.toString()
            }
        })

        binding.taskCreateListButton.text = viewModel.task.value?.list
        binding.taskCreateListButton.setOnClickListener {
            showTaskListNamesMenu(binding.taskCreateListButton)
        }
        ViewCompat
            .setOnApplyWindowInsetsListener(binding.root) { view, insets ->
                val ins = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                if (datePickerDialog == null || ins == 0)
                    view.updatePadding(bottom = ins)
                insets
            }

        setFragmentResultListener(DatePickerDialog.REQUEST_KEY) { key, bundle ->
            viewModel.task.value = bundle.parcelable(DatePickerDialog.BUNDLE_KEY_TASK)
            updateCalendarChip()
        }
        return binding.root
    }

    private fun showTaskListNamesMenu(button: Button) {
        val popup = PopupMenu(requireContext(), button)
        viewModel.taskListNames?.forEach {
            popup.menu.add(it)
        }

        popup.setOnMenuItemClickListener { menuItem ->
            button.text = menuItem.title
            viewModel.task.value?.list = menuItem.title.toString()
            true
        }
        popup.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        binding.taskCreateEditText.windowInsetsController?.show(WindowInsets.Type.ime())
    }

    private fun showDatePickerDialog() {
        datePickerDialog = DatePickerDialog {
            try {
                binding.taskCreateEditText.requestFocus()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    binding.taskCreateEditText.windowInsetsController?.show(WindowInsets.Type.ime())
                }
            } catch (ignore: NullPointerException) {
            }

            datePickerDialog = null
        }
        datePickerDialog!!.arguments = bundleOf(BUNDLE_KEY_TASK to viewModel.task.value)
        datePickerDialog!!.show(parentFragmentManager, DatePickerDialog.TAG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.taskCreateEditText.windowInsetsController?.hide(WindowInsets.Type.ime())
        }
        binding.taskCreateEditText.clearFocus()
        binding.taskCreateDescriptionEditText.clearFocus()
    }

    private fun updateCalendarChip() {
        if (viewModel.task.value?.calendar == null) {
            binding.taskCreateCalendarChip.visibility = View.GONE
        } else {
            binding.taskCreateCalendarChip.visibility = View.VISIBLE
            binding.taskCreateCalendarChip.text =
                viewModel.getCalendarChipText(requireContext(), DateFormat.is24HourFormat(context))
        }

    }

    private fun restoreViewState() {
        val icon = when (viewModel.task.value?.priority) {
            Task.Priority.NO -> ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_priority_high_0)
            Task.Priority.LOW -> ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_priority_high_1)
            Task.Priority.MEDIUM -> ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_priority_high_2)
            Task.Priority.HIGH -> ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_priority_high_3)
            else -> throw NullPointerException("Task priority must not be null")
        }

        binding.taskCreatePriorityButton.setCompoundDrawablesWithIntrinsicBounds(icon,
            null,
            null,
            null)
    }

    @SuppressLint("RestrictedApi")
    private fun showPriorityPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.priority_menu, popup.menu)

        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems)
                if (item.icon != null) item.icon = getInsetIcon(item.icon!!)
        }

        popup.setOnMenuItemClickListener { menuItem ->
            binding.taskCreatePriorityButton.setCompoundDrawablesWithIntrinsicBounds(menuItem.icon,
                null,
                null,
                null)
            viewModel.task.value?.priority = when (menuItem.itemId) {
                R.id.priority_0 -> Task.Priority.NO
                R.id.priority_1 -> Task.Priority.LOW
                R.id.priority_2 -> Task.Priority.MEDIUM
                else -> Task.Priority.HIGH
            }
            true
        }

        popup.show()
    }

    private fun getInsetIcon(drawable: Drawable): InsetDrawable =
        InsetDrawable(drawable, iconMarginPx, 0, iconMarginPx, 0)

    companion object {
        const val TAG = "TaskCreateBottomSheet"
        const val BUNDLE_KEY_TASK = DatePickerDialog.BUNDLE_KEY_TASK
        const val BUNDLE_KEY_LIST = "BundleKeyList"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        datePickerDialog = null
        _binding = null
    }
}