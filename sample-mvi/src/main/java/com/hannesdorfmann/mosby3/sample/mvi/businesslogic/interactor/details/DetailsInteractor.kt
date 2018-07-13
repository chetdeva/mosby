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

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.ShoppingCart
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http.ProductBackendApiDecorator
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.ProductDetail
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.Arrays

/**
 * Interactor that is responsible to load product details
 *
 * @author Hannes Dorfmann
 */
class DetailsInteractor(
        private val backendApi: ProductBackendApiDecorator,
        private val shoppingCart: ShoppingCart
) {

    private fun getProductWithShoppingCartInfo(productId: Int): Observable<ProductDetail> {
        val observables = Arrays.asList(backendApi.getProduct(productId), shoppingCart.itemsInShoppingCart())

        return Observable.combineLatest(observables) { objects ->
            val product = objects[0] as Product
            val productsInShoppingCart = objects[1] as List<Product>
            var inShoppingCart = false
            for (p in productsInShoppingCart) {
                if (p.id == productId) {
                    inShoppingCart = true
                    break
                }
            }

            ProductDetail(product, inShoppingCart)
        }
    }

    /**
     * Get the details of a given product
     */
    fun getDetails(productId: Int): Observable<ProductDetailsViewState> {
        return getProductWithShoppingCartInfo(productId)
                .subscribeOn(Schedulers.io())
                .map { ProductDetailsViewState.DataState(it) }
                .cast(ProductDetailsViewState::class.java)
                .startWith(ProductDetailsViewState.LoadingState)
                .onErrorReturn { ProductDetailsViewState.ErrorState(it) }
    }

    fun addToShoppingCart(product: Product): Completable {
        return shoppingCart.addProduct(product)
    }

    fun removeFromShoppingCart(product: Product): Completable {
        return shoppingCart.removeProduct(product)
    }
}
