package com.example.boris.atmlocator

data class Atm(val name: String, val latitude: Double, val longitude: Double,
               val address: Address, var distance: Float? = null)

data class Address(val formatted: String)
