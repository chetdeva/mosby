/*
 * Copyright 2016 Hannes Dorfmann.
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

package com.hannesdorfmann.mosby3.sample.mvi.view.search

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.search.SearchInteractor
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.search.SearchViewState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * MVI Presenter for [SearchView]
 *
 * @author Hannes Dorfmann
 */
class SearchPresenter(
        private val searchInteractor: SearchInteractor
) : MviBasePresenter<SearchView, SearchViewState>(SearchViewState.SearchNotStartedYet) {

    override fun bindIntents() {
        val search = intent { it.searchIntent() }
                .doOnNext { s -> Timber.d("intent: Search '%s'", s) }
                .switchMap<SearchViewState> { searchInteractor.search(it) }
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(search) { obj, viewState -> obj.render(viewState) }
    }
}
