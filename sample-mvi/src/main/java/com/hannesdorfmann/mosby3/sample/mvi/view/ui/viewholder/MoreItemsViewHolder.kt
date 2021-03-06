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

package com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.AdditionalItemsLoadable

/**
 * @author Hannes Dorfmann
 */
class MoreItemsViewHolder(itemView: View, listener: LoadItemsClickListener) : RecyclerView.ViewHolder(itemView) {

    private val moreItemsCount: TextView
        get() = itemView.findViewById(R.id.moreItemsCount)
    private val loadingView: View
        get() = itemView.findViewById(R.id.loadingView)
    private val loadMoreButton: View
        get() = itemView.findViewById(R.id.loadMoreButtton)
    private val errorRetry: Button
        get() = itemView.findViewById(R.id.errorRetryButton)

    private var currentItem: AdditionalItemsLoadable? = null

    interface LoadItemsClickListener {
        fun loadItemsForCategory(category: String)
    }

    init {
        itemView.setOnClickListener {
            listener.loadItemsForCategory(currentItem?.categoryName ?: "")
        }
        errorRetry.setOnClickListener {
            listener.loadItemsForCategory(currentItem?.categoryName ?: "")
        }
        loadMoreButton.setOnClickListener {
            listener.loadItemsForCategory(currentItem?.categoryName ?: "")
        }
    }

    fun bind(item: AdditionalItemsLoadable) {
        this.currentItem = item

        when {
            item.isLoading -> {
                // TransitionManager.beginDelayedTransition((ViewGroup) itemView);
                moreItemsCount.visibility = View.GONE
                loadMoreButton.visibility = View.GONE
                loadingView.visibility = View.VISIBLE
                errorRetry.visibility = View.GONE
                itemView.isClickable = false
            }
            item.loadingError != null -> {
                //TransitionManager.beginDelayedTransition((ViewGroup) itemView);
                moreItemsCount.visibility = View.GONE
                loadMoreButton.visibility = View.GONE
                loadingView.visibility = View.GONE
                errorRetry.visibility = View.VISIBLE
                itemView.isClickable = true
            }
            else -> {
                moreItemsCount.text = "+" + item.moreItemsCount
                moreItemsCount.visibility = View.VISIBLE
                loadMoreButton.visibility = View.VISIBLE
                loadingView.visibility = View.GONE
                errorRetry.visibility = View.GONE
                itemView.isClickable = true
            }
        }
    }

    companion object {

        fun create(layoutInflater: LayoutInflater,
                   clickListener: LoadItemsClickListener): MoreItemsViewHolder {
            return MoreItemsViewHolder(
                    layoutInflater.inflate(R.layout.item_more_available, null, false), clickListener)
        }
    }
}
