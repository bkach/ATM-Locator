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
class BackgroundTaskRunner {
    private val disposables: CompositeDisposable = CompositeDisposable()

    fun run(backgroundTask: () -> Unit, mainThreadTask: () -> Unit) {
        disposables.add(Completable.fromAction { backgroundTask() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { mainThreadTask() })
    }

    fun dispose() {
        disposables.dispose()
    }
}