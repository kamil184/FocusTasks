package com.kamil184.focustasks.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kamil184.focustasks.databinding.FragmentTasksBinding

class TasksFragment : Fragment() {

    private lateinit var tasksViewModel: TasksViewModel
    private var _binding: FragmentTasksBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        tasksViewModel =
            ViewModelProvider(this).get(TasksViewModel::class.java)

        _binding = FragmentTasksBinding.inflate(inflater, container, false)

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