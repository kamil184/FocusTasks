package com.kamil184.focustasks.ui.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.model.Repeat

class RepeatViewModel : ViewModel() {

    val repeat = MutableLiveData<Repeat?>()

}