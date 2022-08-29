package com.kamil184.focustasks.ui.tasks

import android.annotation.SuppressLint
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.TaskCreateBinding
import java.util.*


class TaskCreateBottomSheet : BottomSheetDialogFragment() {
    private var _binding: TaskCreateBinding? = null
    private val binding get() = _binding!!
    private var datePickerDialog: DatePickerDialog? = null

    private lateinit var viewModel: TaskCreateViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = TaskCreateBinding.inflate(inflater, container, false)
        viewModel =
            ViewModelProvider(this).get(TaskCreateViewModel::class.java)

        binding.taskCreatePriorityButton.setOnClickListener {
            showPriorityPopup(it)
        }

        binding.taskCreateCalendarButton.setOnClickListener {
            datePickerDialog = DatePickerDialog {
                binding.taskCreateEditText.requestFocus()
                binding.taskCreateEditText.windowInsetsController?.show(WindowInsets.Type.ime())
                datePickerDialog = null
            }
            datePickerDialog!!.show(parentFragmentManager, DatePickerDialog.TAG)
            binding.taskCreateEditText.windowInsetsController?.hide(WindowInsets.Type.ime())
            binding.taskCreateEditText.clearFocus()
            binding.taskCreateDescriptionEditText.clearFocus()
        }
        ViewCompat
            .setOnApplyWindowInsetsListener(binding.root) { view, insets ->
                val ins = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                if (datePickerDialog == null || ins == 0)
                    view.updatePadding(bottom = ins)
                insets
            }

        setFragmentResultListener(DatePickerDialog.REQUEST_KEY) { key, bundle ->
            viewModel.task.value?.repeat = bundle.getParcelable(DatePickerDialog.BUNDLE_KEY_REPEAT)
            viewModel.task.value?.calendar = bundle.getSerializable(DatePickerDialog.BUNDLE_KEY_CALENDAR) as Calendar
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.taskCreateEditText.windowInsetsController?.show(WindowInsets.Type.ime())
    }

    @SuppressLint("RestrictedApi")
    private fun showPriorityPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.priority_menu, popup.menu)

        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        0F, context?.resources?.displayMetrics).toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                    } else {
                        item.icon =
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }

        popup.setOnMenuItemClickListener { menuItem ->
            binding.taskCreatePriorityButton.setCompoundDrawablesWithIntrinsicBounds(menuItem.icon,
                null,
                null,
                null)
            true
        }

        popup.show()
    }

    companion object {
        const val TAG = "TaskCreateBottomSheet"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        datePickerDialog?.dismiss()
        _binding = null
    }
}