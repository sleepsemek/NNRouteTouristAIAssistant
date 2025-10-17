package com.sleepsemek.nnroutetouristaiassistant.data.models

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class CoordinateAdapter : TypeAdapter<Coordinate>() {
    override fun write(out: JsonWriter, value: Coordinate) {
        out.value("POINT (${value.longitude} ${value.latitude})")
    }

    override fun read(reader: JsonReader): Coordinate {
        val pointString = reader.nextString()
        return Coordinate.fromString(pointString)
    }
}

@JsonAdapter(CoordinateAdapter::class)
data class Coordinate(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        fun fromString(pointString: String): Coordinate {
            val regex = """POINT\s*\(([\d.]+)\s+([\d.]+)\)""".toRegex()
            val matchResult = regex.find(pointString)

            return if (matchResult != null) {
                val longitude = matchResult.groupValues[1].toDouble()
                val latitude = matchResult.groupValues[2].toDouble()
                Coordinate(latitude, longitude)
            } else {
                throw IllegalArgumentException("Invalid POINT format: $pointString")
            }
        }
    }
}