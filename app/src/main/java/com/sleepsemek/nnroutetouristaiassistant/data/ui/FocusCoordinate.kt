package com.sleepsemek.nnroutetouristaiassistant.data.ui

import com.sleepsemek.nnroutetouristaiassistant.data.models.Coordinate

data class FocusCoordinate(val coordinate: Coordinate, val trigger: Long = System.currentTimeMillis())
