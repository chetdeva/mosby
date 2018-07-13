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

import android.app.Activity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.view.detail.ProductDetailsActivity
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.ShoppingCartItemViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */

class ShoppingCartOverviewAdapter(
        private val activity: Activity
) : RecyclerView.Adapter<ShoppingCartItemViewHolder>(), ShoppingCartItemViewHolder.ItemSelectedListener {

    private val layoutInflater: LayoutInflater = activity.layoutInflater
    private var items: List<ShoppingCartOverviewItem>? = null
    private val selectedProducts = PublishSubject.create<List<Product>>()

    private val isInSelectionMode: Boolean
        get() {
            for (item in items!!) {
                if (item.isSelected) return true
            }

            return false
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartItemViewHolder {
        return ShoppingCartItemViewHolder.create(layoutInflater, this)
    }

    override fun onBindViewHolder(holder: ShoppingCartItemViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size
    }

    override fun onItemClicked(product: ShoppingCartOverviewItem?) {
        if (isInSelectionMode) {
            toggleSelection(product)
        } else {
            ProductDetailsActivity.start(activity, product!!.product)
        }
    }

    override fun onItemLongPressed(product: ShoppingCartOverviewItem?): Boolean {
        toggleSelection(product)
        return true
    }

    fun setItems(items: List<ShoppingCartOverviewItem>) {
        val beforeItems = this.items
        this.items = items
        if (beforeItems == null) {
            notifyDataSetChanged()
        } else {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return beforeItems.size
                }

                override fun getNewListSize(): Int {
                    return items.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return beforeItems[oldItemPosition]
                            .product == items[newItemPosition].product
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return beforeItems[oldItemPosition] == items[newItemPosition]
                }
            })
            diffResult.dispatchUpdatesTo(this)
        }
    }

    private fun toggleSelection(toToggle: ShoppingCartOverviewItem?) {
        val selectedItems = ArrayList<Product>()
        for (item in items!!) {

            if (item == toToggle) {
                if (!toToggle.isSelected) {
                    selectedItems.add(item.product)
                }
            } else if (item.isSelected) {
                selectedItems.add(item.product)
            }
        }

        selectedProducts.onNext(selectedItems)
    }

    fun selectedItemsObservable(): Observable<List<Product>> {
        return selectedProducts.doOnNext { selected -> Timber.d("selected %s ", selected) }
    }

    fun getProductAt(position: Int): Product {
        return items!![position].product
    }
}
