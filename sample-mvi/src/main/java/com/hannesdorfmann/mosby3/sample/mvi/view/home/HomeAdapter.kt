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

package com.hannesdorfmann.mosby3.sample.mvi.view.home

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.AdditionalItemsLoadable
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.SectionHeader
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.LoadingViewHolder
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.MoreItemsViewHolder
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.ProductViewHolder
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.SectionHeaderViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Hannes Dorfmann
 */

class HomeAdapter(
        private val layoutInflater: LayoutInflater,
        private val productClickedListener: ProductViewHolder.ProductClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), MoreItemsViewHolder.LoadItemsClickListener {

    private var isLoadingNextPage = false
    // Use Diff utils
    var items: List<FeedItem>? = null
        set(newItems) {
            val oldItems = this.items
            field = newItems

            if (oldItems == null) {
                notifyDataSetChanged()
            } else {
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return oldItems.size
                    }

                    override fun getNewListSize(): Int {
                        return newItems?.size ?: 0
                    }

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems?.get(newItemPosition)

                        if (oldItem is Product
                                && newItem is Product
                                && oldItem.id == newItem.id) {
                            return true
                        }

                        if (oldItem is SectionHeader
                                && newItem is SectionHeader
                                && oldItem.name == newItem.name) {
                            return true
                        }

                        return (oldItem is AdditionalItemsLoadable
                                && newItem is AdditionalItemsLoadable
                                && oldItem.categoryName == newItem.categoryName)

                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems?.get(newItemPosition)

                        return oldItem == newItem
                    }
                }, true).dispatchUpdatesTo(this)
            }
        }

    private val loadMoreItemsOfCategoryObservable = AtomicReference(PublishSubject.create<String>())

    /**
     * @return true if value has changed since last invocation
     */
    fun setLoadingNextPage(loadingNextPage: Boolean): Boolean {
        val hasLoadingMoreChanged = loadingNextPage != isLoadingNextPage

        val notifyInserted = loadingNextPage && hasLoadingMoreChanged
        val notifyRemoved = !loadingNextPage && hasLoadingMoreChanged
        isLoadingNextPage = loadingNextPage

        if (notifyInserted) {
            notifyItemInserted(this.items!!.size)
        } else if (notifyRemoved) {
            notifyItemRemoved(this.items!!.size)
        }

        return hasLoadingMoreChanged
    }

    fun isLoadingNextPage(): Boolean {
        return isLoadingNextPage
    }

    override fun getItemViewType(position: Int): Int {

        if (isLoadingNextPage && position == this.items!!.size) {
            return VIEW_TYPE_LOADING_MORE_NEXT_PAGE
        }

        val item = this.items!![position]

        if (item is Product) {
            return VIEW_TYPE_PRODUCT
        } else if (item is SectionHeader) {
            return VIEW_TYPE_SECTION_HEADER
        } else if (item is AdditionalItemsLoadable) {
            return VIEW_TYPE_MORE_ITEMS_AVAILABLE
        }

        throw IllegalArgumentException("Not able to dertermine the view type for item at position "
                + position
                + ". Item is: "
                + item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_PRODUCT -> return ProductViewHolder.create(layoutInflater, productClickedListener)
            VIEW_TYPE_LOADING_MORE_NEXT_PAGE -> return LoadingViewHolder.create(layoutInflater)
            VIEW_TYPE_MORE_ITEMS_AVAILABLE -> return MoreItemsViewHolder.create(layoutInflater, this)
            VIEW_TYPE_SECTION_HEADER -> return SectionHeaderViewHolder.create(layoutInflater)
        }

        throw IllegalArgumentException("Couldn't create a ViewHolder for viewType  = $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is LoadingViewHolder) {
            return
        }

        val item = this.items!![position]
        when (holder) {
            is ProductViewHolder -> holder.bind(item as Product)
            is SectionHeaderViewHolder -> holder.onBind(item as SectionHeader)
            is MoreItemsViewHolder -> holder.bind(item as AdditionalItemsLoadable)
            else -> throw IllegalArgumentException("couldn't accept  ViewHolder $holder")
        }
    }

    override fun getItemCount(): Int {
        return if (this.items == null) 0 else this.items!!.size + if (isLoadingNextPage) 1 else 0
    }

    override fun loadItemsForCategory(category: String) {
        loadMoreItemsOfCategoryObservable.get().onNext(category)
    }

    fun loadMoreItemsOfCategoryObservable(): Observable<String> {
        return loadMoreItemsOfCategoryObservable.get()
    }

    companion object {
        const val VIEW_TYPE_PRODUCT = 0
        const val VIEW_TYPE_LOADING_MORE_NEXT_PAGE = 1
        const val VIEW_TYPE_SECTION_HEADER = 2
        const val VIEW_TYPE_MORE_ITEMS_AVAILABLE = 3
    }
}
