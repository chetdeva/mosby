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

package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.search

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product

/**
 * This class represents the ViewState Model for searching
 *
 * @author Hannes Dorfmann
 */
sealed class SearchViewState {

    /**
     * The search has not been stared yet
     */
    object SearchNotStartedYet : SearchViewState()

    object Loading : SearchViewState()

    /**
     * Indicates that the search has delivered an empty result set
     */
    class EmptyResult(private val searchQueryText: String) : SearchViewState()

    /**
     * A valid search result. Contains a list of items that have matched the searching criteria.
     */
    class SearchResult(private val searchQueryText: String, val result: List<Product>): SearchViewState()

    /**
     * Says that an error has occurred while searching
     */
    data class Error(val searchQueryText: String, val error: Throwable) : SearchViewState()
}
