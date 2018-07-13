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

package com.hannesdorfmann.mosby3.sample.mvi.view.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.SectionHeader

/**
 * @author Hannes Dorfmann
 */
class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val sectionName: TextView
        get() = itemView.findViewById(R.id.sectionName)

    fun onBind(item: SectionHeader) {
        sectionName.text = item.name
    }

    companion object {

        fun create(layoutInflater: LayoutInflater): SectionHeaderViewHolder {
            return SectionHeaderViewHolder(
                    layoutInflater.inflate(R.layout.item_section_header, null, false))
        }
    }
}
