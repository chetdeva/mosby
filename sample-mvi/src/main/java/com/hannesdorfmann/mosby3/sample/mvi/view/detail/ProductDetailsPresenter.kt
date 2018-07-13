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

package com.hannesdorfmann.mosby3.sample.mvi.view.detail

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.details.DetailsInteractor
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.details.ProductDetailsViewState
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */

class ProductDetailsPresenter(
        private val interactor: DetailsInteractor
) : MviBasePresenter<ProductDetailsView, ProductDetailsViewState>() {

    override fun bindIntents() {

        intent { it.addToShoppingCartIntent() }
                .doOnNext { product -> Timber.d("intent: add to shopping cart %s", product) }
                .flatMap { product -> interactor.addToShoppingCart(product).toObservable<Any>() }.subscribe()

        intent { it.removeFromShoppingCartIntent() }
                .doOnNext { product -> Timber.d("intent: remove from shopping cart %s", product) }
                .flatMap { product -> interactor.removeFromShoppingCart(product).toObservable<Any>() }
                .subscribe()

        val loadDetails = intent { it.loadDetailsIntent() }
                .doOnNext { productId -> Timber.d("intent: load details for product id = %s", productId) }
                .flatMap { interactor.getDetails(it) }
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(loadDetails) { obj, state -> obj.render(state) }
    }
}
