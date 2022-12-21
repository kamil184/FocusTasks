package com.kamil184.focustasks.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kamil184.focustasks.databinding.FragmentTasksBinding

class TasksFragment : Fragment() {

    private val viewModel: TasksViewModel by viewModels()

    private var _binding: FragmentTasksBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        ViewCompat
            .setOnApplyWindowInsetsListener(binding.tasksTabLayout) { view, insets ->
                view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
                insets
            }

        binding.tasksFab.setOnClickListener {
            val taskCreateBottomSheet = TaskCreateBottomSheet()
            taskCreateBottomSheet.show(parentFragmentManager, TaskCreateBottomSheet.TAG)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}