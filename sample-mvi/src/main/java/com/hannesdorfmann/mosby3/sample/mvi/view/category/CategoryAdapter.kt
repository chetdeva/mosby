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

package com.hannesdorfmann.mosby3.sample.mvi.view.category

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.ProductViewHolder

/**
 * Adapter display search results
 *
 * @author Hannes Dorfmann
 */
class CategoryAdapter(
        private val inflater: LayoutInflater,
        private val productClickedListener: ProductViewHolder.ProductClickedListener
) : RecyclerView.Adapter<ProductViewHolder>() {

    var products: List<Product> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder.create(inflater, productClickedListener)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int {
        return products.size
    }
}
