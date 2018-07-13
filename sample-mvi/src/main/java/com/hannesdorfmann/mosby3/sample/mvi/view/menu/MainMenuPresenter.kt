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

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http.ProductBackendApiDecorator
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.MainMenuItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList
import timber.log.Timber

/**
 * @author Hannes Dorfmann
 */
class MainMenuPresenter(
        private val backendApi: ProductBackendApiDecorator
) : MviBasePresenter<MainMenuView, MenuViewState>() {

    override fun bindIntents() {

        val loadCategories = intent { it.loadCategoriesIntent() }
                .doOnNext { categoryName -> Timber.d("intent: load category %s", categoryName) }
                .flatMap { ignored -> backendApi.allCategories.subscribeOn(Schedulers.io()) }

        val selectCategory = intent { it.selectCategoryIntent() }
                .doOnNext { categoryName -> Timber.d("intent: select category %s", categoryName) }
                .startWith(MainMenuItem.HOME)

        val allIntents = ArrayList<Observable<*>>(2)
        allIntents.add(loadCategories)
        allIntents.add(selectCategory)

        val menuViewStateObservable = Observable.combineLatest(allIntents,
                Function<Array<Any>, MenuViewState> { objects ->
                    val categories = objects[0] as List<String>
                    val selectedCategory = objects[1] as String

                    val categoriesItems = ArrayList<MainMenuItem>(categories.size + 1)
                    categoriesItems.add(
                            MainMenuItem(MainMenuItem.HOME, selectedCategory == MainMenuItem.HOME))

                    for (i in categories.indices) {
                        val category = categories[i]
                        categoriesItems.add(MainMenuItem(category, category == selectedCategory))
                    }

                    MenuViewState.DataState(categoriesItems)
                })
                .startWith(MenuViewState.LoadingState())
                .onErrorReturn { MenuViewState.ErrorState(it) }
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(menuViewStateObservable) { obj, menuViewState -> obj.render(menuViewState) }
    }

    public override fun getViewStateObservable(): Observable<MenuViewState> {
        return super.getViewStateObservable()
    }
}
