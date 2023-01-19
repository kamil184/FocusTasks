package com.kamil184.focustasks.ui.timer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kamil184.focustasks.databinding.FragmentTimerBinding
import com.kamil184.focustasks.data.model.Timer
import com.kamil184.focustasks.data.model.TimerState
import com.kamil184.focustasks.service.TimerService

class TimerFragment : Fragment() {

    private val viewModel: TimerViewModel by viewModels{TimerViewModel.Factory}
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var countDownTimer: CountDownTimer? = null
    private val timerObserver = Observer<Timer> { timer ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        timer.state.observe(viewLifecycleOwner) {
            when (it) {
                TimerState.Running -> {
                    countDownTimer?.cancel()
                    countDownTimer =
                        object : CountDownTimer(timer.timeRemaining.value!!.toLong(), 10) {
                            override fun onFinish() {
                                viewModel.onTimerFinished()
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                viewModel.updateTimeRemaining(millisUntilFinished)
                            }
                        }.start()
                }

                TimerState.Paused -> countDownTimer?.cancel()

                TimerState.Stopped -> {
                    countDownTimer?.cancel()
                    viewModel.onTimerFinished()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.timer.observe(viewLifecycleOwner, timerObserver)
        removeBackgroundTimer()
        viewModel.fetchTimer()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveTimer()
        if (viewModel.timer.value?.state?.value == TimerState.Running) {
            countDownTimer?.cancel()
            viewModel.timer.removeObserver(timerObserver) //иначе успеет создаться countDownTimer при следующем запуске
            startBackgroundTimer()
        }
    }

    private fun startBackgroundTimer() {
        val timerServiceIntent = Intent(requireContext(), TimerService::class.java)
        timerServiceIntent.action = TimerService.ACTION_START

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(timerServiceIntent)
        } else requireContext().startService(timerServiceIntent)
    }

    private fun removeBackgroundTimer() {
        val timerServiceIntent = Intent(requireContext(), TimerService::class.java)
        requireContext().stopService(timerServiceIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}