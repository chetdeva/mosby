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

package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model

/**
 * This is a indicator that also some more items are available that could be loaded
 *
 * @author Hannes Dorfmann
 */
data class AdditionalItemsLoadable(
        val moreItemsCount: Int,
        val categoryName: String,
        val isLoading: Boolean,
        val loadingError: Throwable?
) : FeedItem {

    fun getMoreItemsAvailableCount(): Int {
        return moreItemsCount
    }
}
