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

package com.hannesdorfmann.mosby3.sample.mvi

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.MainMenuItem
import com.hannesdorfmann.mosby3.sample.mvi.view.category.CategoryFragment
import com.hannesdorfmann.mosby3.sample.mvi.view.home.HomeFragment
import com.hannesdorfmann.mosby3.sample.mvi.view.menu.MenuViewState
import com.hannesdorfmann.mosby3.sample.mvi.view.search.SearchFragment
import com.hannesdorfmann.mosby3.sample.mvi.view.selectedcounttoolbar.SelectedCountToolbar
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var title: String? = null
    private lateinit var disposable: Disposable
    private lateinit var clearSelectionRelay: PublishSubject<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.title = "Mosby MVI"
        toolbar.inflateMenu(R.menu.activity_main_toolbar)
        toolbar.setOnMenuItemClickListener { item ->

            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.drawerLayout, SearchFragment())
                    .addToBackStack("Search")
                    .commit()
            true
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            showCategoryItems(MainMenuItem.HOME)
        } else {
            title = savedInstanceState.getString(KEY_TOOLBAR_TITLE)
            toolbar.title = title
        }

        // TODO Create a Presenter & ViewState for this Activity
        val dependencyInjection = SampleApplication.getDependencyInjection(this)
        disposable = dependencyInjection.mainMenuPresenter
                .viewStateObservable
                .filter { state -> state is MenuViewState.DataState }
                .cast(MenuViewState.DataState::class.java)
                .map { findSelectedMenuItem(it) }
                .subscribe { this.showCategoryItems(it) }
        clearSelectionRelay = dependencyInjection.clearSelectionRelay
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        Timber.d("------- Destroyed -------")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Timber.d("Activity onSaveInstanceState()")
        outState.putString(KEY_TOOLBAR_TITLE, toolbar.title.toString())
    }

    private fun findSelectedMenuItem(state: MenuViewState.DataState): String {
        for (item in state.categories)
            if (item.isSelected) return item.name

        throw IllegalStateException("No category is selected in Main Menu$state")
    }

    override fun onBackPressed() {
        val selectedCountToolbar = findViewById(R.id.selectedCountToolbar) as SelectedCountToolbar
        if (!closeDrawerIfOpen()) {
            if (selectedCountToolbar.visibility == View.VISIBLE) {
                clearSelectionRelay.onNext(true)
            } else if (!closeSlidingUpPanelIfOpen()) super.onBackPressed()
        }
    }

    private fun closeSlidingUpPanelIfOpen(): Boolean {
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return true
        }
        return false
    }

    private fun closeDrawerIfOpen(): Boolean {
        val drawer = findViewById(R.id.drawerLayout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }

    private fun showCategoryItems(categoryName: String) {
        closeDrawerIfOpen()
        val currentCategory = toolbar.title.toString()
        if (currentCategory != categoryName) {
            toolbar.title = categoryName
            val f: Fragment = if (categoryName == MainMenuItem.HOME) {
                HomeFragment()
            } else {
                CategoryFragment.newInstance(categoryName)
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, f).commit()
        }
    }

    companion object {
        private val KEY_TOOLBAR_TITLE = "toolbarTitle"
    }
}
