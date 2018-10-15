# ATM Locator

An app which shows a list of ATMs closest to the users' location

<img src="screenshot.png" alt="screenshot" width="300" />

## Description

The app contains two fragments, a Map Fragment and a custom fragment housing the RecyclerView.

The model layer is connected to the view layer through a View Model (MVVM). A series of tests are written for the Repository, but could be expanded to encompass the entire app.

Some edge cases are covered (user rejects location, network not found, etc.) but there are many which are not. A few examples are if the location moves, or if the network is regained under a session. Given more time, these would be relatively trivial to implement.	

## Built With

* [Kotlin](https://kotlinlang.org/) - The language used in the project
* [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) - Used with LiveData in order to attach data to the activity/fragment lifecycle
* [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Used to make data be Lifecycle aware, and helps persist data through the activity Lifecycle 
* [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Lifecycle aware model, which is used to create controllers that are attached to a given activity/fragment
* [Mockito](https://site.mockito.org/) - Used for mocking and injecting mock classes in tests
* [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView) - Used to display the list of ATMs
* [Retrofit](https://square.github.io/retrofit/) - Used to simplify networking
* [Room](https://developer.android.com/topic/libraries/architecture/room) - Used for persistance in a SQLite database
* [Android Design Support Library](https://developer.android.com/topic/libraries/support-library/packages) - Used for the snackbar, which shows errors
* [ConstraintLayout](https://developer.android.com/reference/android/support/constraint/ConstraintLayout) - Layout used to provide complex UIs with a minimal view hierarchy
* [Koin](https://github.com/InsertKoinIO/koin) - Used for dependency injection
* [Mockito-Kotlin](https://github.com/nhaarman/mockito-kotlin/wiki) - A library used to make mockito more "kotlin-aware". Used primarily to be able to pass null objects into Mockito mocks.

## Contributors

This app was written by:

	Boris Kachscovsky
	kachscovsky@gmail.com
	+46764281780
