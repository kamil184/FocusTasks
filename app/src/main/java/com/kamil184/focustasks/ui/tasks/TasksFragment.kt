package com.kamil184.focustasks.ui.tasks

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.databinding.EditTextDialogBinding
import com.kamil184.focustasks.databinding.FragmentTasksBinding
import com.kamil184.focustasks.ui.dialogs.DatePickerDialog
import com.kamil184.focustasks.ui.dialogs.TaskCreateBottomSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class TasksFragment : Fragment() {

    private val viewModel: TasksViewModel by viewModels { TasksViewModel.Factory }

    private var _binding: FragmentTasksBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: TasksViewPagerAdapter

    private fun TabLayout.selectedTabText() =
        getTabAt(selectedTabPosition)?.text.toString()

    override fun onStart() {
        super.onStart()
        viewModel.fetchTaskListNames()
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveTaskListNames()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        ViewCompat
            .setOnApplyWindowInsetsListener(binding.root) { view, insets ->
                view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
                insets
            }

        viewPagerAdapter = TasksViewPagerAdapter()
        binding.tasksViewPager.adapter = viewPagerAdapter
        binding.tasksViewPager.offscreenPageLimit = 1

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.taskListNames.collectLatest { taskListNames ->
                        binding.tasksCreateTabButton.visibility = View.VISIBLE
                        viewPagerAdapter.submitTaskListNames(taskListNames)
                        if (binding.tasksTabLayout.tabCount == 0) {
                            taskListNames.forEach {
                                val tab = binding.tasksTabLayout.newTab()
                                tab.text = it
                                binding.tasksTabLayout.addTab(tab)
                            }
                        }
                    }
                }
                launch {
                    viewModel.tasks.collectLatest {
                        viewPagerAdapter.submitTasks(it)
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.tasksTabsAppBar.menu.setGroupDividerEnabled(true)
        }

        TabLayoutMediator(binding.tasksTabLayout, binding.tasksViewPager) { tab, position ->
            tab.text = viewModel.taskListNames.value[position]
        }.attach()

        binding.tasksFab.setOnClickListener {
            val taskCreateBottomSheet = TaskCreateBottomSheet()
            val task = Task()
            task.list = binding.tasksTabLayout.selectedTabText()
            taskCreateBottomSheet.arguments =
                bundleOf(BUNDLE_KEY_TASK to task, BUNDLE_KEY_LIST to viewModel.taskListNames.value)
            taskCreateBottomSheet.show(parentFragmentManager, TaskCreateBottomSheet.TAG)
        }

        binding.tasksTabsAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.tasks_menu_delete_list -> {
                    if (viewModel.taskListNames.value.size != 1) {
                        val text = binding.tasksTabLayout.selectedTabText()
                        val isSuccess = viewModel.removeTaskListName(text)
                        if (!isSuccess) showSnackbar(R.string.something_went_wrong)
                    } else showSnackbar(R.string.the_only_existing_list_cannot_be_deleted)
                    true
                }
                R.id.tasks_menu_rename_list -> {
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    val editTextDialogBinding =
                        EditTextDialogBinding.inflate(inflater, container, false)
                    editTextDialogBinding.editTextDialogTextInputLayout.setHint(R.string.list_name)
                    val editText = editTextDialogBinding.editTextDialogEditText
                    val textBefore = binding.tasksTabLayout.selectedTabText()
                    editText.setText(textBefore)
                    editText.setSelection(textBefore.length)
                    builder.setView(editTextDialogBinding.root)
                    builder.setPositiveButton(R.string.dialog_positive_button_text) { dialogInterface, i ->
                        val text = editText.text.toString()
                        val position = binding.tasksTabLayout.selectedTabPosition
                        val isSuccess = viewModel.setTaskListName(position, text)
                        if (!isSuccess)
                            showSnackbar(R.string.this_list_already_exists)
                    }

                    builder.setNegativeButton(R.string.dialog_negative_button_text, null)
                    val dialog = builder.create()
                    dialog.show()
                    editText.setOnEditorActionListener { textView, actionId, keyEvent ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            val text = editText.text.toString()
                            val position = binding.tasksTabLayout.selectedTabPosition
                            val isSuccess = viewModel.setTaskListName(position, text)
                            if (isSuccess) dialog.cancel()
                            else showSnackbar(R.string.this_list_already_exists)
                            return@setOnEditorActionListener true
                        }
                        false
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        editText.windowInsetsController?.show(WindowInsets.Type.ime())
                    }
                    true
                }
                else -> false
            }
        }

        binding.tasksCreateTabButton.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            val editTextDialogBinding = EditTextDialogBinding.inflate(inflater, container, false)
            val editText = editTextDialogBinding.editTextDialogEditText
            editTextDialogBinding.editTextDialogTextInputLayout.setHint(R.string.add_a_list)

            builder.setView(editTextDialogBinding.root)
            builder.setPositiveButton(R.string.dialog_positive_button_text) { dialogInterface, i ->
                val text = editText.text.toString()
                val isSuccess = viewModel.addTaskListName(text)
                if (!isSuccess) showSnackbar(R.string.this_list_already_exists)
            }

            builder.setNegativeButton(R.string.dialog_negative_button_text, null)
            val dialog = builder.create()
            dialog.show()
            editText.setOnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val text = textView.text.toString()
                    val isSuccess = viewModel.addTaskListName(text)
                    if (isSuccess) showSnackbar(R.string.this_list_already_exists)
                    else dialog.cancel()
                    return@setOnEditorActionListener true
                }
                false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                editText.windowInsetsController?.show(WindowInsets.Type.ime())
            }
        }
        return binding.root
    }

    private fun showSnackbar(@StringRes textRes: Int) =
        Snackbar.make(binding.root, getString(textRes),
            Snackbar.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val BUNDLE_KEY_TASK = DatePickerDialog.BUNDLE_KEY_TASK
        const val BUNDLE_KEY_LIST = TaskCreateBottomSheet.BUNDLE_KEY_LIST
    }
}