/*
 * ATMLocator
 * Copyright (C) 2018 Boris Kachscovsky
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.boris.atmlocator.repository

import android.arch.persistence.room.Room
import android.content.Context
import android.support.annotation.VisibleForTesting
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Wrapper class used to abstract the context away from the Repository in order to make the Repository
 * testable
 */
@VisibleForTesting
open class AtmDatabaseFactory : KoinComponent {

    private val context: Context by inject()

    open fun getDatabase() : AtmDatabase {
        return Room.databaseBuilder(context, AtmDatabase::class.java, "atm-db").build()
    }
}