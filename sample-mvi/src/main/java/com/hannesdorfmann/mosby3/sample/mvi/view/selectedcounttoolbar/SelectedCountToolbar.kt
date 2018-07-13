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

package com.hannesdorfmann.mosby3.sample.mvi.view.selectedcounttoolbar

import android.content.Context
import android.os.Parcelable
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.hannesdorfmann.mosby3.ViewGroupMviDelegate
import com.hannesdorfmann.mosby3.ViewGroupMviDelegateCallback
import com.hannesdorfmann.mosby3.ViewGroupMviDelegateImpl
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */

class SelectedCountToolbar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs),
        SelectedCountToolbarView, ViewGroupMviDelegateCallback<SelectedCountToolbarView, SelectedCountToolbarPresenter> {

    private val mviDelegate = ViewGroupMviDelegateImpl(this, this, true)

    private val clearSelectionIntent = PublishSubject.create<Boolean>()
    private val deleteSelectedItemsIntent = PublishSubject.create<Boolean>()

    init {
        setNavigationOnClickListener { v -> clearSelectionIntent.onNext(true) }
        setNavigationIcon(R.drawable.ic_back_selection_count_toolbar)
        inflateMenu(R.menu.shopping_cart_toolbar)
        setOnMenuItemClickListener { item ->
            deleteSelectedItemsIntent.onNext(true)
            true
        }
    }

    override fun clearSelectionIntent(): Observable<Boolean> {
        return clearSelectionIntent
    }

    override fun deleteSelectedItemsIntent(): Observable<Boolean> {
        return deleteSelectedItemsIntent
    }

    override fun getMvpView(): SelectedCountToolbarView {
        return this
    }

    override fun createPresenter(): SelectedCountToolbarPresenter {
        Timber.d("create presenter")
        return SampleApplication.getDependencyInjection(context)
                .newSelectedCountToolbarPresenter()
    }

    override fun render(selectedCount: Int) {
        Timber.d("render %d selected items", selectedCount)
        if (selectedCount == 0) {
            if (visibility == View.VISIBLE) {
                animate().alpha(0f).withEndAction { visibility = View.GONE }.start()
            } else {
                visibility = View.GONE
            }
        } else {
            title = resources.getQuantityString(R.plurals.items, selectedCount, selectedCount)

            if (visibility != View.VISIBLE) {
                animate().alpha(1f).withStartAction { visibility = View.VISIBLE }.start()
            } else {
                visibility = View.VISIBLE
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mviDelegate.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mviDelegate.onDetachedFromWindow()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        return mviDelegate.onSaveInstanceState()
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        mviDelegate.onRestoreInstanceState(state)
    }

    override fun superOnSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun superOnRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(state)
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
        // Don't needed for this view
    }

}
