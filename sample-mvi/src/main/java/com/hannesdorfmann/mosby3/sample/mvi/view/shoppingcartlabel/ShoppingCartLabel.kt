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

package com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartlabel

import android.content.Context
import android.os.Parcelable
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import com.hannesdorfmann.mosby3.ViewGroupMviDelegate
import com.hannesdorfmann.mosby3.ViewGroupMviDelegateCallback
import com.hannesdorfmann.mosby3.ViewGroupMviDelegateImpl
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import io.reactivex.Observable
import timber.log.Timber

/**
 * A UI widget that displays how many items are in the shopping cart
 * @author Hannes Dorfmann
 */
class ShoppingCartLabel(context: Context, attrs: AttributeSet) : AppCompatButton(context, attrs),
        ShoppingCartLabelView, ViewGroupMviDelegateCallback<ShoppingCartLabelView, ShoppingCartLabelPresenter> {

    private val mviDelegate = ViewGroupMviDelegateImpl(this, this, true)

    override fun getMvpView(): ShoppingCartLabelView {
        return this
    }

    override fun createPresenter(): ShoppingCartLabelPresenter {
        Timber.d("create presenter")
        return SampleApplication.getDependencyInjection(context).newShoppingCartLabelPresenter()
    }

    override fun superOnSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun superOnRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
    }

    override fun loadIntent(): Observable<Boolean> {
        return Observable.just(true)
    }

    override fun render(itemsInShoppingCart: Int) {
        Timber.d("render %d items in shopping cart", itemsInShoppingCart)
        text = resources.getQuantityString(R.plurals.items, itemsInShoppingCart, itemsInShoppingCart)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mviDelegate.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mviDelegate.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return mviDelegate.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        mviDelegate.onRestoreInstanceState(state)
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        // Not needed for this view
    }
}
