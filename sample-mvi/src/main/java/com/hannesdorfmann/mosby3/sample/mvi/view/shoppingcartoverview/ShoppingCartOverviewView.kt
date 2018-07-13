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

import com.hannesdorfmann.mosby3.mvp.MvpView
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import io.reactivex.Observable

/**
 * A View that displays all the items in your shopping cart.
 *
 * @author Hannes Dorfmann
 */
interface ShoppingCartOverviewView : MvpView {

    /**
     * Intent to load the items from the shopping cart
     */
    fun loadItemsIntent(): Observable<Boolean>

    /**
     * Intent to mark a given item as selected
     */
    fun selectItemsIntent(): Observable<List<Product>>

    /**
     * Intent to remove a given item from the shopping cart
     */
    fun removeItemIntent(): Observable<Product>

    /**
     * Renders the View with the given items that are in the shopping cart right now
     */
    fun render(itemsInShoppingCart: List<ShoppingCartOverviewItem>)
}
