package com.kamil184.focustasks.ui.timer

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kamil184.focustasks.databinding.FragmentTimerBinding
import com.kamil184.focustasks.model.Timer
import com.kamil184.focustasks.model.TimerState

class TimerFragment : Fragment() {

    private lateinit var viewModel: TimerViewModel
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var countDownTimer: CountDownTimer? = null
    private val timerObserver = Observer<Timer> { timer ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        timer.state.observe(viewLifecycleOwner, {
            when (it) {
                TimerState.Running ->
                    countDownTimer = object : CountDownTimer(timer.timeRemaining.value!!.toLong(), 10) {
                        override fun onFinish() {
                            viewModel.onTimerFinished()
                        }

                        override fun onTick(millisUntilFinished: Long) {
                            viewModel.updateTimeRemaining(millisUntilFinished)
                        }
                    }.start()

                TimerState.Paused -> countDownTimer?.cancel()

                TimerState.Stopped -> {
                    countDownTimer?.cancel()
                    viewModel.onTimerFinished()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel =
            ViewModelProvider(this).get(TimerViewModel::class.java)
        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        viewModel.timer.observe(viewLifecycleOwner, timerObserver)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.timer.observe(viewLifecycleOwner, timerObserver)
        //TODO: remove background timer, hide notification
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveTimerState()
        if (viewModel.timer.value!!.state.value == TimerState.Running) {
            countDownTimer?.cancel()
            viewModel.timer.removeObserver(timerObserver) //иначе успеет создаться countDownTimer
            //TODO: start background timer and show notification
        } else if (viewModel.timer.value!!.state.value == TimerState.Paused) {
            //TODO: show notification
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}