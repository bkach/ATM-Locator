package com.example.boris.atmlocator

import com.example.boris.atmlocator.util.BackgroundTaskRunner

class MockBackgroundTaskRunner : BackgroundTaskRunner() {
    var backgroundTask: (() -> Unit)? = null
    var mainThreadTask: (() -> Unit)? = null

    override fun run(backgroundTask: () -> Unit, mainThreadTask: () -> Unit) {
        this.backgroundTask = backgroundTask
        this.mainThreadTask = mainThreadTask
    }
}