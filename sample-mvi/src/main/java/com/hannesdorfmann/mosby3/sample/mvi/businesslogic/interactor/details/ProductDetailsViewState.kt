/*
 * Copyright 2017 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.details

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.ProductDetail

/**
 * The View state for the Menu
 *
 * @author Hannes Dorfmann
 */
sealed class ProductDetailsViewState {

    /**
     * Loads the list of all menu items
     */
    object LoadingState : ProductDetailsViewState()

    /**
     * Ane error has ocurred while loading the data
     */
    data class ErrorState(val error: Throwable) : ProductDetailsViewState()

    /**
     * Data has been loaded successfully and can now be displayed
     */
    data class DataState(val detail: ProductDetail) : ProductDetailsViewState()
}
