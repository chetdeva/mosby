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

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product

/**
 * @author Hannes Dorfmann
 */

sealed class PartialStateChanges {

    /**
     * Indicates that the first page is loading
     */
    object FirstPageLoading : PartialStateChanges()

    /**
     * Indicates that an error has occurred while loading the first page
     */
    data class FirstPageError(val error: Throwable) : PartialStateChanges()

    /**
     * Indicates that the first page data has been loaded successfully
     */
    data class FirstPageLoaded(val data: List<FeedItem>) : PartialStateChanges()

    /**
     * Next Page has been loaded successfully
     */
    data class NextPageLoaded(val data: List<FeedItem>) : PartialStateChanges()

    /**
     * Error while loading new page
     */
    data class NexPageLoadingError(val error: Throwable) : PartialStateChanges()

    /**
     * Indicates that loading the next page has started
     */
    object NextPageLoading : PartialStateChanges()

    /**
     * Indicates that loading the newest items via pull to refresh has started
     */
    object PullToRefreshLoading : PartialStateChanges()

    /**
     * Indicates that an error while loading the newest items via pull to refresh has occurred
     */
    data class PullToRefeshLoadingError(val error: Throwable) : PartialStateChanges()

    /**
     * Indicates that data has been loaded successfully over pull-to-refresh
     */
    data class PullToRefreshLoaded(val data: List<FeedItem>) : PartialStateChanges()

    /**
     * Loading all Products of a given category has been started
     */
    data class ProductsOfCategoryLoading(val categoryName: String) : PartialStateChanges()

    /**
     * An error while loading all products has been occurred
     */
    data class ProductsOfCategoryLoadingError(val categoryName: String, val error: Throwable) : PartialStateChanges()

    /**
     * Products of a given Category has been loaded
     */
    data class ProductsOfCategoryLoaded(val categoryName: String, val data: List<Product>) : PartialStateChanges()
}
