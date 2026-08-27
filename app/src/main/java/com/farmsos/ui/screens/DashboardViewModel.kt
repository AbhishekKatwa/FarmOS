package com.farmsos.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.DashboardSnapshot
import com.farmsos.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
data class DashboardUiState(val loading:Boolean=true,val error:String?=null,val snapshot:DashboardSnapshot?=null,val days:Int=1)
@HiltViewModel class DashboardViewModel @Inject constructor(private val repo:DashboardRepository):ViewModel(){private val _state=MutableStateFlow(DashboardUiState());val state=_state.asStateFlow();init{load(1)};fun load(days:Int){viewModelScope.launch{_state.value=_state.value.copy(loading=true,error=null,days=days);val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val end=f.format(Date());val c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,1-days);repo.load(null,null,null,f.format(c.time),end).fold({_state.value=DashboardUiState(false,null,it,days)},{_state.value=DashboardUiState(false,it.message,null,days)})}}}
