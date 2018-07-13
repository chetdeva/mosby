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

package com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartoverview

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.ShoppingCart
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */
class ShoppingCartOverviewPresenter(
        private val shoppingCart: ShoppingCart,
        private val deleteSelectedItemsIntent: Observable<Boolean>,
        private val clearSelectionIntent: Observable<Boolean>
) : MviBasePresenter<ShoppingCartOverviewView, List<ShoppingCartOverviewItem>>() {

    private var deleteDisposable: Disposable? = null
    private var deleteSelectedDisposable: Disposable? = null

    override fun bindIntents() {

        //
        // Observable that emits a list of selected products over time (or empty list if the selection has been cleared)
        //
        val selectedItemsIntent = intent { it.selectItemsIntent() }
                .mergeWith(clearSelectionIntent.map { ignore -> emptyList<Product>() })
                .doOnNext { items -> Timber.d("intent: selected items %d", items.size) }
                .startWith(ArrayList<Product>(0))

        //
        // Delete multiple selected Items
        //

        deleteSelectedDisposable = selectedItemsIntent
                .switchMap { selectedItems ->
                    deleteSelectedItemsIntent.filter { ignored -> !selectedItems.isEmpty() }
                            .doOnNext { ignored -> Timber.d("intent: remove %d selected items from shopping cart", selectedItems.size) }
                            .flatMap { ignore -> shoppingCart.removeProducts(selectedItems).toObservable<Any>() }
                }
                .subscribe()

        //
        // Delete a single item
        //
        deleteDisposable = intent { it.removeItemIntent() }
                .doOnNext { item -> Timber.d("intent: remove item from shopping cart: %s", item) }
                .flatMap { productToDelete -> shoppingCart.removeProduct(productToDelete).toObservable<Any>() }
                .subscribe()
        //
        // Display a list of items in the shopping cart
        //
        val shoppingCartContentObservable = intent { it.loadItemsIntent() }
                .doOnNext { ignored -> Timber.d("load ShoppingCart intent") }
                .flatMap { ignored -> shoppingCart.itemsInShoppingCart() }


        //
        // Display list of items / view state
        //
        val combiningObservables = Arrays.asList<Observable<*>>(shoppingCartContentObservable, selectedItemsIntent)

        val shoppingCartContentWithSelectedItems = Observable.combineLatest<Any, List<ShoppingCartOverviewItem>>(combiningObservables) { results ->
            val itemsInShoppingCart = results[0] as List<Product>
            val selectedProducts = results[1] as List<Product>

            val items = ArrayList<ShoppingCartOverviewItem>(itemsInShoppingCart.size)
            for (i in itemsInShoppingCart.indices) {
                val p = itemsInShoppingCart[i]
                items.add(ShoppingCartOverviewItem(p, selectedProducts.contains(p)))
            }
            items
        }
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(shoppingCartContentWithSelectedItems) { obj, itemsInShoppingCart -> obj.render(itemsInShoppingCart) }
    }

    override fun unbindIntents() {
        deleteDisposable?.dispose()
        deleteSelectedDisposable?.dispose()
    }

    public override fun getViewStateObservable(): Observable<List<ShoppingCartOverviewItem>> {
        return super.getViewStateObservable()
    }
}
