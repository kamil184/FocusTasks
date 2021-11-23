package com.kamil184.focustasks.ui.timer

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kamil184.focustasks.databinding.FragmentTimerBinding
import com.kamil184.focustasks.manager.TimerManager
import com.kamil184.focustasks.model.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerFragment : Fragment() {

    private lateinit var viewModel: TimerViewModel
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel =
            ViewModelProvider(this).get(TimerViewModel::class.java)

        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        binding.timer = viewModel.timer
        binding.lifecycleOwner = this

        Log.d("Timer", "onCreateView: ${logText()}")

        viewModel.timer.state.observe(viewLifecycleOwner, {
            Log.d("Timer", "timer state observe: ${logText()}")
            when (it) {
                TimerState.Running ->
                    timer = object : CountDownTimer(viewModel.timer.timeRemaining.value!!.toLong(), 10) {
                        override fun onFinish() {
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.onTimerFinished()
                            }
                        }

                        override fun onTick(millisUntilFinished: Long) {
                            viewModel.updateTimeRemaining(millisUntilFinished)
                        }
                    }.start()

                TimerState.Paused -> timer?.cancel()

                TimerState.Stopped -> {
                    timer?.cancel()
                    viewModel.onTimerFinished()
                }

            }
        })
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Log.d("Timer", "onStart: ${logText()}")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Timer", "onResume: ${logText()}")
        //TODO: remove background timer, hide notification
    }

    override fun onPause() {
        super.onPause()
        Log.d("Timer", "onPause: ${logText()}")

        if (viewModel.timer.state.value == TimerState.Running) {
            timer?.cancel()
            //TODO: start background timer and show notification
        } else if (viewModel.timer.state.value == TimerState.Paused) {
            //TODO: show notification
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveTimerState()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("Timer", "onStop: ${logText()}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Timer", "onDestroyView: ${logText()}")
        _binding = null
    }

    fun logText():String = "binding: ${binding.timer.hashCode()} vm: ${viewModel.timer.hashCode()}"
}