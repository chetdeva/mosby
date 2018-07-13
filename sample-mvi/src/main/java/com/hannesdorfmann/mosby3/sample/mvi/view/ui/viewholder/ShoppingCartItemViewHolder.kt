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

package com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http.ProductBackendApi
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.dependencyinjection.DependencyInjection
import com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartoverview.ShoppingCartOverviewItem
import java.util.Locale

/**
 * @author Hannes Dorfmann
 */

class ShoppingCartItemViewHolder private constructor(
        itemView: View,
        private val selectedListener: ItemSelectedListener
) : RecyclerView.ViewHolder(itemView) {

    private var item: ShoppingCartOverviewItem? = null
    private val selectedDrawable: Drawable
    @BindView(R.id.image)
    @JvmField var image: ImageView? = null
    @BindView(R.id.name)
    @JvmField var name: TextView? = null
    @BindView(R.id.price)
    @JvmField var price: TextView? = null

    interface ItemSelectedListener {
        fun onItemClicked(product: ShoppingCartOverviewItem?)

        fun onItemLongPressed(product: ShoppingCartOverviewItem?): Boolean
    }

    init {
        itemView.setOnClickListener { v -> selectedListener.onItemClicked(item) }
        itemView.setOnLongClickListener { v -> selectedListener.onItemLongPressed(item) }
        selectedDrawable = ColorDrawable(
                itemView.context.resources.getColor(R.color.selected_shopping_cart_item))

        ButterKnife.bind(this, itemView)
    }

    fun bind(item: ShoppingCartOverviewItem) {
        this.item = item
        val product = item.product

        Glide.with(itemView.context)
                .load(DependencyInjection.BASE_IMAGE_URL + product.image)
                .centerCrop()
                .into(image!!)

        name?.text = product.name
        price?.text = String.format(Locale.US, "$ %.2f", product.price)

        if (item.isSelected) {
            if (Build.VERSION.SDK_INT >= 23) {
                itemView.foreground = selectedDrawable
            } else {
                itemView.background = selectedDrawable
            }
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                itemView.foreground = null
            } else {
                itemView.background = null
            }
        }
    }

    companion object {

        fun create(inflater: LayoutInflater,
                   selectedListener: ItemSelectedListener): ShoppingCartItemViewHolder {
            return ShoppingCartItemViewHolder(
                    inflater.inflate(R.layout.item_shopping_cart, null, false), selectedListener)
        }
    }
}
