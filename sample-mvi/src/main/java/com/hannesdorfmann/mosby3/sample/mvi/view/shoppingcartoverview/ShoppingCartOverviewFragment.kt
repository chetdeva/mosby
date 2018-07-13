/*
 * Copyright 2017 Hannes Dorfmann.
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

package com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartoverview

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.Product
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_shopping_cart.*
import java.util.concurrent.TimeUnit
import timber.log.Timber

/**
 * This class doesn't neccessarily has to be a fragment. It's just a fragment because I want to
 * demonstrate that mosby works with fragments in xml layouts too.
 *
 * @author Hannes Dorfmann
 */
class ShoppingCartOverviewFragment : MviFragment<ShoppingCartOverviewView, ShoppingCartOverviewPresenter>(), ShoppingCartOverviewView {

    private lateinit var adapter: ShoppingCartOverviewAdapter
    private val removeRelay = PublishSubject.create<Product>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_shopping_cart, container, false)
        adapter = ShoppingCartOverviewAdapter(activity)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shoppingCartRecyclerView.adapter = adapter
        shoppingCartRecyclerView.layoutManager = LinearLayoutManager(activity)
        setUpItemTouchHelper()
    }

    override fun createPresenter(): ShoppingCartOverviewPresenter {
        Timber.d("Create Presenter")
        return SampleApplication.getDependencyInjection(activity).shoppingCartPresenter
    }

    override fun loadItemsIntent(): Observable<Boolean> {
        return Observable.just(true)
    }

    override fun selectItemsIntent(): Observable<List<Product>> {
        return adapter.selectedItemsObservable()
    }

    private fun setUpItemTouchHelper() {

        //
        // Borrowed from https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/blob/master/app/src/main/java/net/nemanjakovacevic/recyclerviewswipetodelete/MainActivity.java
        //

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            private lateinit var background: Drawable
            private lateinit var xMark: Drawable
            private var xMarkMargin: Int = 0
            private var initiated: Boolean = false

            private fun init() {
                background = ColorDrawable(ContextCompat.getColor(activity, R.color.delete_background))
                xMark = ContextCompat.getDrawable(activity, R.drawable.ic_remove)
                xMarkMargin = activity.resources.getDimension(R.dimen.ic_clear_margin).toInt()
                initiated = true
            }

            // not important, we don't want drag & drop
            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val swipedPosition = viewHolder.adapterPosition
                val productAt = adapter.getProductAt(swipedPosition)
                removeRelay.onNext(productAt)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                                     viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int,
                                     isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.adapterPosition == -1) {
                    // not interested in those
                    return
                }

                if (!initiated) {
                    init()
                }

                // draw red background
                background.setBounds(itemView.right + dX.toInt(), itemView.top,
                        itemView.right, itemView.bottom)
                background.draw(c)

                // draw x mark
                val itemHeight = itemView.bottom - itemView.top
                val intrinsicWidth = xMark.intrinsicWidth
                val intrinsicHeight = xMark.intrinsicWidth

                val xMarkLeft = itemView.right - xMarkMargin - intrinsicWidth
                val xMarkRight = itemView.right - xMarkMargin
                val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val xMarkBottom = xMarkTop + intrinsicHeight
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)

                // xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        val mItemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        mItemTouchHelper.attachToRecyclerView(shoppingCartRecyclerView)
    }

    override fun removeItemIntent(): Observable<Product> {
        // DELEAY it for 500 miliseconds so that the user sees the swipe to delete animation
        return removeRelay.delay(500, TimeUnit.MILLISECONDS)
    }

    override fun render(itemsInShoppingCart: List<ShoppingCartOverviewItem>) {
        Timber.d("Render %s ", itemsInShoppingCart)
        adapter!!.setItems(itemsInShoppingCart)
    }
}
