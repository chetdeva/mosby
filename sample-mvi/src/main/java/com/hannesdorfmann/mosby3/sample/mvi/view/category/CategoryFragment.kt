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

import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.view.detail.ProductDetailsActivity
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.GridSpacingItemDecoration
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.ProductViewHolder
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.include_errorview.*
import timber.log.Timber

/**
 * Displays all products of a certain category on the screen
 *
 * @author Hannes Dorfmann
 */
class CategoryFragment : MviFragment<CategoryView, CategoryPresenter>(),
        CategoryView, ProductViewHolder.ProductClickedListener {

    private lateinit var adapter: CategoryAdapter

    override fun onProductClicked(product: Product) {
        ProductDetailsActivity.start(activity, product)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        adapter = CategoryAdapter(inflater, this)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spanCount = resources.getInteger(R.integer.grid_span_size)
        val layoutManager = GridLayoutManager(activity, spanCount)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount,
                resources.getDimensionPixelSize(R.dimen.grid_spacing), true))
    }

    override fun createPresenter(): CategoryPresenter {
        Timber.d("Create presenter")
        return SampleApplication.getDependencyInjection(context).newCategoryPresenter()
    }

    override fun loadIntents(): Observable<String> {
        return Observable.just(arguments.getString(CATEGORY_NAME))
    }

    override fun render(state: CategoryViewState) {
        Timber.d("Render %s", state)
        when (state) {
            is CategoryViewState.LoadingState -> renderLoading()
            is CategoryViewState.DataState -> renderData(state.products)
            is CategoryViewState.ErrorState -> renderError()
        }
    }

    private fun renderError() {
        TransitionManager.beginDelayedTransition((view as ViewGroup))
        loadingView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun renderLoading() {
        TransitionManager.beginDelayedTransition((view as ViewGroup))
        loadingView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun renderData(products: List<Product>) {
        adapter.products = products
        adapter.notifyDataSetChanged()
        TransitionManager.beginDelayedTransition((view as ViewGroup))
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    companion object {

        private const val CATEGORY_NAME = "categoryName"

        fun newInstance(categoryName: String): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putString(CATEGORY_NAME, categoryName)
            fragment.arguments = args
            return fragment
        }
    }
}
