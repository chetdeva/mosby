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

package com.hannesdorfmann.mosby3.sample.mvi.view.home

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.Arrays
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import org.junit.Assert

/**
 * This class is responsible to drive the HomeView.
 * Internally this creates a [HomeView] and attaches it to the [HomePresenter]
 * and offers public API to fire view intents and to check for expected view.render() events.
 *
 *
 *
 * **Create a new instance for every unit test**
 *
 *
 * @author Hannes Dorfmann
 */
class HomeViewRobot(private val presenter: HomePresenter) {

    private val loadFirstPageSubject = PublishSubject.create<Boolean>()
    private val loadNextPageSubject = PublishSubject.create<Boolean>()
    private val pullToRefreshSubject = PublishSubject.create<Boolean>()
    private val loadAllProductsFromCategorySubject = PublishSubject.create<String>()
    private val renderEvents = CopyOnWriteArrayList<HomeViewState>()
    private val renderEventSubject = ReplaySubject.create<HomeViewState>()

    private val view = object : HomeView {
        override fun loadFirstPageIntent(): Observable<Boolean> {
            return loadFirstPageSubject
        }

        override fun loadNextPageIntent(): Observable<Boolean> {
            return loadNextPageSubject
        }

        override fun pullToRefreshIntent(): Observable<Boolean> {
            return pullToRefreshSubject
        }

        override fun loadAllProductsFromCategoryIntent(): Observable<String> {
            return loadAllProductsFromCategorySubject
        }

        override fun render(viewState: HomeViewState) {
            renderEvents.add(viewState)
            renderEventSubject.onNext(viewState)
        }
    }

    init {
        presenter.attachView(view)
    }

    fun fireLoadFirstPageIntent() {
        loadFirstPageSubject.onNext(true)
    }

    fun fireLoadNextPageIntent() {
        loadNextPageSubject.onNext(true)
    }

    fun firePullToRefreshIntent() {
        pullToRefreshSubject.onNext(true)
    }

    fun fireLoadAllProductsFromCategory(category: String) {
        loadAllProductsFromCategorySubject.onNext(category)
    }

    /**
     * Blocking waits for view.render() calls and
     *
     * @param expectedHomeViewStates The expected  HomeViewStates that will be passed to
     * view.render()
     */
    fun assertViewStateRendered(vararg expectedHomeViewStates: HomeViewState) {

        if (expectedHomeViewStates == null) {
            throw NullPointerException("expectedHomeViewStates == null")
        }

        val eventsCount = expectedHomeViewStates.size
        renderEventSubject.take(eventsCount.toLong())
                .timeout(10, TimeUnit.SECONDS)
                .blockingSubscribe()

        /*
    // Wait for few milli seconds to ensure that no more render events have occurred
    // before finishing the test and checking expectations (asserts)
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    */

        if (renderEventSubject.values.size > eventsCount) {
            Assert.fail("Expected to wait for "
                    + eventsCount
                    + ", but there were "
                    + renderEventSubject.values.size
                    + " Events in total, which is more than expected: "
                    + arrayToString(renderEventSubject.values))
        }

        Assert.assertEquals(Arrays.asList(*expectedHomeViewStates), renderEvents)
    }

    /**
     * Simple helper function to print the content of an array as a string
     */
    private fun arrayToString(array: Array<Any>): String {
        val buffer = StringBuffer()
        for (o in array) {
            buffer.append(o.toString())
            buffer.append("\n")
        }

        return buffer.toString()
    }
}
