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

package com.example.boris.atmlocator.util

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Utility class to run tasks in the background
 *
 * Keeps a reference to all background tasks and disposes of them once the lifecycle of the
 * BackgroundTaskRunner's parent is over.
 *
 * It is the responsibility of those using this class to ensure that all disposables are properly
 * disposed, otherwise this could cause a memory leak
 */
open class BackgroundTaskRunner {
    private val disposables: CompositeDisposable = CompositeDisposable()

    open fun run(backgroundTask: () -> Unit, mainThreadTask: () -> Unit) {
        disposables.add(Completable.fromAction { backgroundTask() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { mainThreadTask() })
    }

    fun dispose() {
        disposables.dispose()
    }
}