/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anmei.warehouseputaway.ui.putawayitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.anmei.warehouseputaway.data.PutawayItemRepository
import com.anmei.warehouseputaway.ui.putawayitem.PutawayItemUiState.Error
import com.anmei.warehouseputaway.ui.putawayitem.PutawayItemUiState.Loading
import com.anmei.warehouseputaway.ui.putawayitem.PutawayItemUiState.Success
import javax.inject.Inject

@HiltViewModel
class PutawayItemViewModel @Inject constructor(
    private val putawayItemRepository: PutawayItemRepository
) : ViewModel() {

    val uiState: StateFlow<PutawayItemUiState> = putawayItemRepository
        .putawayItems.map<List<String>, PutawayItemUiState>(::Success)
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addPutawayItem(name: String) {
        viewModelScope.launch {
            putawayItemRepository.add(name)
        }
    }
}

sealed interface PutawayItemUiState {
    object Loading : PutawayItemUiState
    data class Error(val throwable: Throwable) : PutawayItemUiState
    data class Success(val data: List<String>) : PutawayItemUiState
}
