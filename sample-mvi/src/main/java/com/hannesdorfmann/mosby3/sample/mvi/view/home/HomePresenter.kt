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

import android.support.v4.util.Pair
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.feed.HomeFeedLoader
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.AdditionalItemsLoadable
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.SectionHeader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */
class HomePresenter(private val feedLoader: HomeFeedLoader) : MviBasePresenter<HomeView, HomeViewState>() {

    override fun bindIntents() {

        //
        // In a real app this code would rather be moved to an Interactor
        //
        val loadFirstPage = intent { it.loadFirstPageIntent() }
                .doOnNext { ignored -> Timber.d("intent: load first page") }
                .flatMap { ignored ->
                    feedLoader.loadFirstPage()
                            .map { items -> PartialStateChanges.FirstPageLoaded(items) as PartialStateChanges }
                            .startWith(PartialStateChanges.FirstPageLoading)
                            .onErrorReturn { PartialStateChanges.FirstPageError(it) }
                            .subscribeOn(Schedulers.io())
                }

        val nextPage = intent { it.loadNextPageIntent() }.doOnNext { ignored -> Timber.d("intent: load next page") }
                .flatMap { ignored ->
                    feedLoader.loadNextPage()
                            .map { items -> PartialStateChanges.NextPageLoaded(items) as PartialStateChanges }
                            .startWith(PartialStateChanges.NextPageLoading)
                            .onErrorReturn { PartialStateChanges.NexPageLoadingError(it) }
                            .subscribeOn(Schedulers.io())
                }

        val pullToRefresh = intent { it.pullToRefreshIntent() }
                .doOnNext { ignored -> Timber.d("intent: pull to refresh") }
                .flatMap { ignored ->
                    feedLoader.loadNewestPage()
                            .subscribeOn(Schedulers.io())
                            .map { items -> PartialStateChanges.PullToRefreshLoaded(items) as PartialStateChanges }
                            .startWith(PartialStateChanges.PullToRefreshLoading)
                            .onErrorReturn { PartialStateChanges.PullToRefeshLoadingError(it) }
                }

        val loadMoreFromGroup = intent { it.loadAllProductsFromCategoryIntent() }
                .doOnNext { categoryName -> Timber.d("intent: load more from category %s", categoryName) }
                .flatMap { categoryName ->
                    feedLoader.loadProductsOfCategory(categoryName)
                            .subscribeOn(Schedulers.io())
                            .map { products ->
                                PartialStateChanges.ProductsOfCategoryLoaded(
                                        categoryName, products) as PartialStateChanges
                            }
                            .startWith(PartialStateChanges.ProductsOfCategoryLoading(categoryName))
                            .onErrorReturn { error ->
                                PartialStateChanges.ProductsOfCategoryLoadingError(categoryName,
                                        error)
                            }
                }

        val allIntentsObservable = Observable.merge(loadFirstPage, nextPage, pullToRefresh, loadMoreFromGroup)
                .observeOn(AndroidSchedulers.mainThread())

        val initialState = HomeViewState.Builder().firstPageLoading(true).build()

        subscribeViewState(
                allIntentsObservable.scan(initialState) { previousState, partialChanges ->
                    this.viewStateReducer(previousState, partialChanges)
                }
                        .distinctUntilChanged()
        ) { obj, viewState -> obj.render(viewState) }
    }

    private fun viewStateReducer(previousState: HomeViewState,
                                 partialChanges: PartialStateChanges): HomeViewState {

        return when (partialChanges) {
            is PartialStateChanges.FirstPageLoading -> {
                previousState.builder()
                        .firstPageLoading(true)
                        .firstPageError(null).build()
            }
            is PartialStateChanges.FirstPageError -> {
                previousState.builder()
                        .firstPageLoading(false)
                        .firstPageError(partialChanges.error)
                        .build()
            }
            is PartialStateChanges.FirstPageLoaded -> {
                previousState.builder()
                        .firstPageLoading(false)
                        .firstPageError(null)
                        .data(partialChanges.data)
                        .build()
            }
            is PartialStateChanges.NextPageLoading -> {
                previousState.builder()
                        .nextPageLoading(true)
                        .nextPageError(null).build()
            }
            is PartialStateChanges.NexPageLoadingError -> {
                previousState.builder()
                        .nextPageLoading(false)
                        .nextPageError(partialChanges.error)
                        .build()
            }
            is PartialStateChanges.NextPageLoaded -> {
                val data = ArrayList<FeedItem>(previousState.data.size + partialChanges.data.size)
                data.addAll(previousState.data)
                data.addAll(partialChanges.data)

                previousState.builder().nextPageLoading(false).nextPageError(null).data(data).build()
            }
            is PartialStateChanges.PullToRefreshLoading -> {
                previousState.builder()
                        .pullToRefreshLoading(true)
                        .pullToRefreshError(null)
                        .build()
            }
            is PartialStateChanges.PullToRefeshLoadingError -> {
                previousState.builder()
                        .pullToRefreshLoading(false)
                        .pullToRefreshError(
                                partialChanges.error)
                        .build()
            }
            is PartialStateChanges.PullToRefreshLoaded -> {
                val data = ArrayList<FeedItem>(previousState.data.size + partialChanges.data.size)
                data.addAll(partialChanges.data)
                data.addAll(previousState.data)
                previousState.builder()
                        .pullToRefreshLoading(false)
                        .pullToRefreshError(null)
                        .data(data)
                        .build()
            }
            is PartialStateChanges.ProductsOfCategoryLoading -> {
                val found = findAdditionalItems(
                        partialChanges.categoryName,
                        previousState.data)
                val foundItem = found.second
                val toInsert = AdditionalItemsLoadable(foundItem.getMoreItemsAvailableCount(),
                        foundItem.categoryName, true, null)

                val data = ArrayList<FeedItem>(previousState.data.size)
                data.addAll(previousState.data)
                data[found.first] = toInsert

                previousState.builder().data(data).build()
            }
            is PartialStateChanges.ProductsOfCategoryLoadingError -> {
                val found = findAdditionalItems(
                        partialChanges.categoryName,
                        previousState.data)

                val foundItem = found.second
                val toInsert = AdditionalItemsLoadable(foundItem.getMoreItemsAvailableCount(),
                        foundItem.categoryName, false,
                        partialChanges.error)

                val data = ArrayList<FeedItem>(previousState.data.size)
                data.addAll(previousState.data)
                data[found.first] = toInsert

                previousState.builder().data(data).build()
            }
            is PartialStateChanges.ProductsOfCategoryLoaded -> {
                val found = findAdditionalItems(
                        partialChanges.categoryName,
                        previousState.data)

                val data = ArrayList<FeedItem>(previousState.data.size + partialChanges.data.size)
                data.addAll(previousState.data)

                // Search for the section header
                var sectionHeaderIndex = -1
                for (i in found.first downTo 0) {
                    val item = previousState.data[i]
                    if (item is SectionHeader && item.name == partialChanges.categoryName) {
                        sectionHeaderIndex = i
                        break
                    }

                    // Remove all items of that category. The new list of products will be added afterwards
                    data.removeAt(i)
                }

                if (sectionHeaderIndex < 0) {
                    throw RuntimeException("Couldn't find the section header for category " + partialChanges.categoryName)
                }

                data.addAll(sectionHeaderIndex + 1, partialChanges.data)

                previousState.builder().data(data).build()
            }
        }

    }

    /**
     * find the [AdditionalItemsLoadable] for the given category name
     *
     * @param categoryName The name of the category
     * @param items the list of feeditems
     */
    private fun findAdditionalItems(categoryName: String,
                                    items: List<FeedItem>): Pair<Int, AdditionalItemsLoadable> {
        val size = items.size
        for (i in 0 until size) {
            val item = items[i]
            if (item is AdditionalItemsLoadable && item.categoryName == categoryName) {
                return Pair.create(i, item)
            }
        }

        throw RuntimeException("No "
                + AdditionalItemsLoadable::class.java.simpleName
                + " has been found for category = "
                + categoryName)
    }
}
