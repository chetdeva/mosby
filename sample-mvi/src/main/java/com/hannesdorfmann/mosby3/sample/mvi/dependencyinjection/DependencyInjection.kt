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

package com.hannesdorfmann.mosby3.sample.mvi.dependencyinjection

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.ShoppingCart
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.feed.GroupedPagedFeedLoader
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.feed.HomeFeedLoader
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.feed.PagingFeedLoader
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http.ProductBackendApi
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http.ProductBackendApiDecorator
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.details.DetailsInteractor
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.search.SearchInteractor
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.searchengine.SearchEngine
import com.hannesdorfmann.mosby3.sample.mvi.view.category.CategoryPresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.checkoutbutton.CheckoutButtonPresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.detail.ProductDetailsPresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.home.HomePresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.menu.MainMenuPresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.search.SearchPresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.selectedcounttoolbar.SelectedCountToolbarPresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartlabel.ShoppingCartLabelPresenter
import com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartoverview.ShoppingCartOverviewPresenter
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * This is just a very simple example that creates dependency injection.
 * In a real project you might would like to use dagger.
 *
 * @author Hannes Dorfmann
 */
class DependencyInjection {

    // Don't do this in your real app
    val clearSelectionRelay = PublishSubject.create<Boolean>()
    private val deleteSelectionRelay = PublishSubject.create<Boolean>()
    //
    // Some singletons
    //
    private val httpLogger = HttpLoggingInterceptor()
    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().addInterceptor(httpLogger).build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    private val backendApi = retrofit.create(ProductBackendApi::class.java)
    private val backendApiDecorator = ProductBackendApiDecorator(backendApi)
    /**
     * This is a singleton
     */
    val mainMenuPresenter = MainMenuPresenter(backendApiDecorator)
    private val shoppingCart = ShoppingCart()
    /**
     * This is a singleton
     */
    val shoppingCartPresenter = ShoppingCartOverviewPresenter(shoppingCart, deleteSelectionRelay, clearSelectionRelay)


    private fun newSearchEngine(): SearchEngine {
        return SearchEngine(backendApiDecorator)
    }

    private fun newSearchInteractor(): SearchInteractor {
        return SearchInteractor(newSearchEngine())
    }

    internal fun newPagingFeedLoader(): PagingFeedLoader {
        return PagingFeedLoader(backendApiDecorator)
    }

    internal fun newGroupedPagedFeedLoader(): GroupedPagedFeedLoader {
        return GroupedPagedFeedLoader(newPagingFeedLoader())
    }

    internal fun newHomeFeedLoader(): HomeFeedLoader {
        return HomeFeedLoader(newGroupedPagedFeedLoader(), backendApiDecorator)
    }

    fun newSearchPresenter(): SearchPresenter {
        return SearchPresenter(newSearchInteractor())
    }

    fun newHomePresenter(): HomePresenter {
        return HomePresenter(newHomeFeedLoader())
    }

    fun newCategoryPresenter(): CategoryPresenter {
        return CategoryPresenter(backendApiDecorator)
    }

    fun newProductDetailsPresenter(): ProductDetailsPresenter {
        return ProductDetailsPresenter(DetailsInteractor(backendApiDecorator, shoppingCart))
    }

    fun newShoppingCartLabelPresenter(): ShoppingCartLabelPresenter {
        return ShoppingCartLabelPresenter(shoppingCart)
    }

    fun newCheckoutButtonPresenter(): CheckoutButtonPresenter {
        return CheckoutButtonPresenter(shoppingCart)
    }

    fun newSelectedCountToolbarPresenter(): SelectedCountToolbarPresenter {

        val selectedItemCountObservable = shoppingCartPresenter.viewStateObservable.map { items ->
            var selected = 0
            for (item in items) {
                if (item.isSelected) selected++
            }
            selected
        }

        return SelectedCountToolbarPresenter(selectedItemCountObservable, clearSelectionRelay,
                deleteSelectionRelay)
    }

    companion object {

        @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Changeable for unit testing")
        const val BASE_URL = "https://raw.githubusercontent.com"
        const val BASE_URL_BRANCH = "master"
        const val BASE_IMAGE_URL = (BASE_URL
                + "/sockeqwe/mosby/"
                + DependencyInjection.BASE_URL_BRANCH
                + "/sample-mvi/server/images/")
    }
}
