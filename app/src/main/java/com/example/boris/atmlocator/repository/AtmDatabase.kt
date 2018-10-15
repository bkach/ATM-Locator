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

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Room database used to fetch the AtmDao.
 *
 * This class' annotations are also used to communicate with the database, and can be useful
 * when migrating atmsLiveData
 */
@Database(entities = [Atm::class], version = 1, exportSchema = false)
abstract class AtmDatabase : RoomDatabase() {
    abstract fun atmDao(): AtmDao
}