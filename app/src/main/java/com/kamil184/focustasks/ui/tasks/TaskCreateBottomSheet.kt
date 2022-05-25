package com.kamil184.focustasks.ui.tasks

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.DialogInterface
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kamil184.focustasks.R
import com.kamil184.focustasks.databinding.TaskCreateBinding
import com.kamil184.focustasks.util.dpToPx

class TaskCreateBottomSheet : BottomSheetDialogFragment() {
    private var _binding: TaskCreateBinding? = null
    private val binding get() = _binding!!

    private var first = true // shows if the keyboard is opened for the first time
    private lateinit var activityRootView: ConstraintLayout
    private var lastHeightDiff = 0 //onGlobalLayoutListener is used to track changes in layout height.
    // If the previous value is equal to the current one, then we will not consider this case.

    private val onGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener { // determines if the keyboard opens or not
            val heightDiff = activityRootView.rootView.height - activityRootView.height
            if (!first) {
                if (heightDiff < dpToPx(context, 200) && heightDiff != lastHeightDiff) {
                    dialog?.dismiss()
                }
            } else{
                first = false
                binding.taskCreateEditText.requestFocus()
            }
            lastHeightDiff = heightDiff
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = TaskCreateBinding.inflate(inflater, container, false)

        binding.taskCreatePriorityButton.setOnClickListener {
            showPriorityPopup(it)
        }
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        activityRootView = requireActivity().findViewById(R.id.container)

        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.taskCreateEditText.requestFocus()
        first = true
        Log.d(TAG, "onStart Callback first: $first")
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
        Log.d(TAG, "onDestroyView")
        _binding = null
        first = true
        activityRootView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }
}