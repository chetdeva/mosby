package com.hannesdorfmann.mosby3.sample.mvi.view.search

import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.search.SearchViewState
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.view.detail.ProductDetailsActivity
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.GridSpacingItemDecoration
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder.ProductViewHolder
import com.jakewharton.rxbinding2.widget.RxSearchView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.include_errorview.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchFragment : MviFragment<SearchView, SearchPresenter>(),
        SearchView, ProductViewHolder.ProductClickedListener {

    private var adapter: SearchAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        adapter = SearchAdapter(inflater, this)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = adapter
        val spanCount = resources.getInteger(R.integer.grid_span_size)
        recyclerView.layoutManager = GridLayoutManager(activity, spanCount)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount,
                resources.getDimensionPixelSize(R.dimen.grid_spacing), true))
    }

    override fun onProductClicked(product: Product) {
        ProductDetailsActivity.start(activity, product)
    }

    override fun createPresenter(): SearchPresenter {
        Timber.d("createPresenter")
        return SampleApplication.getDependencyInjection(activity).newSearchPresenter()
    }

    override fun searchIntent(): Observable<String> {
        return RxSearchView.queryTextChanges(searchView)
                .skip(2) // Because after screen orientation changes query Text will be resubmitted again
                .filter { queryString -> queryString.length > 3 || queryString.length == 0 }
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .map { it.toString() }
    }

    override fun render(viewState: SearchViewState) {
        Timber.d("render %s", viewState)
        when (viewState) {
            is SearchViewState.SearchNotStartedYet -> renderSearchNotStarted()
            is SearchViewState.Loading -> renderLoading()
            is SearchViewState.SearchResult -> renderResult(viewState.result)
            is SearchViewState.EmptyResult -> renderEmptyResult()
            is SearchViewState.Error -> {
                Timber.e(viewState.error)
                renderError()
            }
        }
    }

    private fun renderResult(result: List<Product>) {
        TransitionManager.beginDelayedTransition(container)
        recyclerView.visibility = View.VISIBLE
        loadingView.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorView.visibility = View.GONE
        adapter?.products = result
        adapter?.notifyDataSetChanged()
    }

    private fun renderSearchNotStarted() {
        TransitionManager.beginDelayedTransition(container)
        recyclerView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }

    private fun renderLoading() {
        TransitionManager.beginDelayedTransition(container)
        recyclerView.visibility = View.GONE
        loadingView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }

    private fun renderError() {
        TransitionManager.beginDelayedTransition(container)
        recyclerView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    private fun renderEmptyResult() {
        TransitionManager.beginDelayedTransition(container)
        recyclerView.visibility = View.GONE
        loadingView.visibility = View.GONE
        errorView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }
}
