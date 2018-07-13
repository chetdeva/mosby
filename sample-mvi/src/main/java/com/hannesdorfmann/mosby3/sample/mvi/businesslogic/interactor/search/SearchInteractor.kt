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

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.searchengine.SearchEngine
import io.reactivex.Observable

/**
 * Interacts with [SearchEngine] to search for items
 *
 * @author Hannes Dorfmann
 */
class SearchInteractor(private val searchEngine: SearchEngine) {

    /**
     * Search for items
     */
    fun search(searchString: String): Observable<SearchViewState> {
        // Empty String, so no search
        return if (searchString == "") {
            Observable.just(SearchViewState.SearchNotStartedYet)
        } else searchEngine.searchFor(searchString)
                .map { products ->
                    if (products.isEmpty()) {
                        SearchViewState.EmptyResult(searchString)
                    } else {
                        SearchViewState.SearchResult(searchString, products)
                    }
                }
                .startWith(SearchViewState.Loading)
                .onErrorReturn { error -> SearchViewState.Error(searchString, error) }

        // search for product
    }
}
