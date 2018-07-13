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

package com.hannesdorfmann.mosby3.sample.mvi.view.menu

import android.content.Context
import android.support.transition.TransitionManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.hannesdorfmann.mosby3.mvi.layout.MviFrameLayout
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import io.reactivex.Observable
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */
class MainMenuLayout(context: Context, attrs: AttributeSet) :
        MviFrameLayout<MainMenuView, MainMenuPresenter>(context, attrs), MainMenuView {

    private val itemView by lazy { View.inflate(context, R.layout.view_mainmenu, this) }
    private val adapter: MainMenuAdapter by lazy { MainMenuAdapter(LayoutInflater.from(context)) }

    private val loadingView: View
        get() = itemView.findViewById(R.id.loadingView)
    private val recyclerView: RecyclerView
        get() = itemView.findViewById(R.id.recyclerView)
    private val errorView: View
        get() = itemView.findViewById(R.id.errorView)

    init {
        recyclerView.adapter = adapter
    }

    override fun createPresenter(): MainMenuPresenter {
        Timber.d("Create MainMenuPresenter")
        return SampleApplication.getDependencyInjection(context).mainMenuPresenter
    }

    override fun loadCategoriesIntent(): Observable<Boolean> {
        return Observable.just(true)
    }

    override fun selectCategoryIntent(): Observable<String> {
        return adapter.selectedItemObservable
    }

    override fun render(menuViewState: MenuViewState) {
        Timber.d("Render %s", menuViewState)

        TransitionManager.beginDelayedTransition(this)
        when (menuViewState) {
            is MenuViewState.LoadingState -> {
                loadingView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                errorView.visibility = View.GONE
            }
            is MenuViewState.DataState -> {
                adapter.setItems(menuViewState.categories)
                adapter.notifyDataSetChanged()
                loadingView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                errorView.visibility = View.GONE
            }
            is MenuViewState.ErrorState -> {
                loadingView.visibility = View.GONE
                recyclerView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
            }
        }
    }
}
