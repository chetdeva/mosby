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

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.transition.TransitionManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindInt
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.view.detail.ProductDetailsActivity
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.GridSpacingItemDecoration
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.ProductViewHolder
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.include_errorview.*
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */

class HomeFragment : MviFragment<HomeView, HomePresenter>(), HomeView, ProductViewHolder.ProductClickedListener {

    private lateinit var adapter: HomeAdapter
    private var layoutManager: GridLayoutManager? = null

    override fun createPresenter(): HomePresenter {
        Timber.d("createPresenter")
        return SampleApplication.getDependencyInjection(activity).newHomePresenter()
    }

    override fun onProductClicked(product: Product) {
        ProductDetailsActivity.start(activity, product)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        adapter = HomeAdapter(inflater, this)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spanCount = resources.getInteger(R.integer.grid_span_size)
        layoutManager = GridLayoutManager(activity, spanCount)
        layoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {

                val viewType = adapter.getItemViewType(position)
                return if (viewType == HomeAdapter.VIEW_TYPE_LOADING_MORE_NEXT_PAGE || viewType == HomeAdapter.VIEW_TYPE_SECTION_HEADER) {
                    spanCount
                } else 1
            }
        }

        /*
    recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount,
        getResources().getDimensionPixelSize(R.dimen.grid_spacing), true));
        */

        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    override fun loadFirstPageIntent(): Observable<Boolean> {
        return Observable.just(true).doOnComplete { Timber.d("firstPage completed") }
    }

    override fun loadNextPageIntent(): Observable<Boolean> {
        return RxRecyclerView.scrollStateChanges(recyclerView)
                .filter { event -> !adapter.isLoadingNextPage() }
                .filter { event -> event == RecyclerView.SCROLL_STATE_IDLE }
                .filter { event -> layoutManager?.findLastCompletelyVisibleItemPosition() == (adapter.items?.size ?: 0 - 1) }
                .map { integer -> true }
    }

    override fun pullToRefreshIntent(): Observable<Boolean> {
        return RxSwipeRefreshLayout.refreshes(swipeRefreshLayout).map { ignored -> true }
    }

    override fun loadAllProductsFromCategoryIntent(): Observable<String> {
        return adapter.loadMoreItemsOfCategoryObservable()
    }

    override fun render(viewState: HomeViewState) {
        Timber.d("render %s", viewState)
        if (!viewState.isLoadingFirstPage && viewState.firstPageError == null) {
            renderShowData(viewState)
        } else if (viewState.isLoadingFirstPage) {
            renderFirstPageLoading()
        } else if (viewState.firstPageError != null) {
            renderFirstPageError()
        } else {
            throw IllegalStateException("Unknown view state $viewState")
        }
    }

    private fun renderShowData(state: HomeViewState) {
        TransitionManager.beginDelayedTransition((view as ViewGroup))
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.VISIBLE
        val changed = adapter.setLoadingNextPage(state.isLoadingNextPage)
        if (changed && state.isLoadingNextPage) {
            // scroll to the end of the list so that the user sees the load more progress bar
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        }
        adapter.items = state.data // TODO error: this must be done before setLoading() otherwise error will occure. see https://github.com/sockeqwe/mosby/issues/244

        val pullToRefreshFinished = (swipeRefreshLayout.isRefreshing
                && !state.isLoadingPullToRefresh
                && state.pullToRefreshError == null)
        if (pullToRefreshFinished) {
            // Swipe to refresh finished successfully so scroll to the top of the list (to see inserted items)
            recyclerView.smoothScrollToPosition(0)
        }

        swipeRefreshLayout.isRefreshing = state.isLoadingPullToRefresh

        if (state.nextPageError != null) {
            Snackbar.make(view!!, R.string.error_unknown, Snackbar.LENGTH_LONG)
                    .show() // TODO callback
        }

        if (state.pullToRefreshError != null) {
            Snackbar.make(view!!, R.string.error_unknown, Snackbar.LENGTH_LONG)
                    .show() // TODO callback
        }
    }

    private fun renderFirstPageLoading() {
        TransitionManager.beginDelayedTransition((view as ViewGroup))
        loadingView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
    }

    private fun renderFirstPageError() {
        TransitionManager.beginDelayedTransition((view as ViewGroup))
        loadingView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }
}
