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

package com.hannesdorfmann.mosby3.sample.mvi.view.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.transition.TransitionManager
import android.view.View
import com.bumptech.glide.Glide
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.details.ProductDetailsViewState
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import com.hannesdorfmann.mosby3.sample.mvi.dependencyinjection.DependencyInjection
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.include_errorview.*
import timber.log.Timber
import java.util.*

class ProductDetailsActivity : MviActivity<ProductDetailsView, ProductDetailsPresenter>(), ProductDetailsView {

    private var product: Product? = null
    private var isProductInshoppingCart = false
    private var fabClickObservable: Observable<Boolean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        fabClickObservable = RxView.clicks(fab!!).share().map { ignored -> true }
    }

    override fun createPresenter(): ProductDetailsPresenter {
        Timber.d("Create presenter")
        return SampleApplication.getDependencyInjection(this).newProductDetailsPresenter()
    }

    override fun loadDetailsIntent(): Observable<Int> {
        return Observable.just(intent.getIntExtra(KEY_PRODUCT_ID, 0))
    }

    override fun addToShoppingCartIntent(): Observable<Product> {
        return fabClickObservable!!.filter { item -> product != null }
                .filter { item -> !isProductInshoppingCart }
                .map { item -> product!! }
    }

    override fun removeFromShoppingCartIntent(): Observable<Product> {
        return fabClickObservable!!.filter { item -> product != null }
                .filter { item -> isProductInshoppingCart }
                .map { item -> product!! }
    }

    override fun render(state: ProductDetailsViewState) {
        Timber.d("render $state")

        when (state) {
            is ProductDetailsViewState.LoadingState -> renderLoading()
            is ProductDetailsViewState.DataState -> renderData(state)
            is ProductDetailsViewState.ErrorState -> renderError()
        }
    }

    private fun renderError() {
        TransitionManager.beginDelayedTransition(root)
        errorView!!.visibility = View.VISIBLE
        loadingView!!.visibility = View.GONE
        detailsView!!.visibility = View.GONE
    }

    private fun renderData(state: ProductDetailsViewState.DataState) {
        TransitionManager.beginDelayedTransition(root)
        errorView!!.visibility = View.GONE
        loadingView!!.visibility = View.GONE
        detailsView!!.visibility = View.VISIBLE

        isProductInshoppingCart = state.detail.isInShoppingCart
        product = state.detail.product
        price!!.text = "Price: $" + String.format(Locale.US, "%.2f", product!!.price)
        description!!.text = product!!.description
        toolbar!!.title = product!!.name
        collapsingToolbar.title = product!!.name

        if (isProductInshoppingCart) {
            fab!!.setImageResource(R.drawable.ic_in_shopping_cart)
        } else {
            fab!!.setImageResource(R.drawable.ic_add_shopping_cart)
        }

        Glide.with(this)
                .load(DependencyInjection.BASE_IMAGE_URL + product!!.image)
                .centerCrop()
                .into(backdrop!!)
    }

    private fun renderLoading() {
        TransitionManager.beginDelayedTransition(root)
        errorView!!.visibility = View.GONE
        loadingView!!.visibility = View.VISIBLE
        detailsView!!.visibility = View.GONE
    }

    companion object {

        val KEY_PRODUCT_ID = "productId"

        fun start(activity: Activity, product: Product) {
            val i = Intent(activity, ProductDetailsActivity::class.java)
            i.putExtra(KEY_PRODUCT_ID, product.id)
            activity.startActivity(i)
        }
    }
}
