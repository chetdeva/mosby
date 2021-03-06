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

package com.hannesdorfmann.mosby3.sample.mvi.view.selectedcounttoolbar

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */
class SelectedCountToolbarPresenter(private val selectedCountObservable: Observable<Int>,
                                    private val clearSelectionRelay: PublishSubject<Boolean>,
                                    private val deleteSelectedItemsRelay: PublishSubject<Boolean>
) : MviBasePresenter<SelectedCountToolbarView, Int>() {

    private var clearSelectionDisposal: Disposable? = null
    private var deleteSelectedItemsDisposal: Disposable? = null

    override fun bindIntents() {

        clearSelectionDisposal = intent { it.clearSelectionIntent() }
                .doOnNext { ignore -> Timber.d("intent: clear selection") }
                .subscribe { aBoolean -> clearSelectionRelay.onNext(aBoolean) }

        deleteSelectedItemsDisposal = intent { it.deleteSelectedItemsIntent() }
                .doOnNext { items -> Timber.d("intent: delete selected items $items") }
                .subscribe { aBoolean -> deleteSelectedItemsRelay.onNext(aBoolean) }

        subscribeViewState(selectedCountObservable) { obj, selectedCount -> obj.render(selectedCount) }
    }

    override fun unbindIntents() {
        clearSelectionDisposal?.dispose()
        deleteSelectedItemsDisposal?.dispose()
    }
}
