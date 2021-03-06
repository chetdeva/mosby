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

package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.feed

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http.ProductBackendApiDecorator
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import io.reactivex.Observable
import java.util.Collections

/**
 * @author Hannes Dorfmann
 */

class PagingFeedLoader(
        private val backend: ProductBackendApiDecorator
) {
    private var currentPage = 1
    private var endReached = false
    private var newestPageLoaded = false

    fun newestPage(): Observable<List<Product>> {
        return if (newestPageLoaded) {
            Observable.fromCallable {
                Thread.sleep(2000)
                emptyList<Product>()
            }
        } else backend.getProducts(0).doOnNext { products -> newestPageLoaded = true }

    }

    fun nextPage(): Observable<List<Product>> {
        // I know, it's not a pure function nor elegant code
        // but that is not the purpose of this demo.
        // This code should be understandable by everyone.

        return if (endReached) {
            Observable.just(emptyList())
        } else backend.getProducts(currentPage).doOnNext { result ->
            currentPage++
            if (result.isEmpty()) {
                endReached = true
            }
        }

    }
}
