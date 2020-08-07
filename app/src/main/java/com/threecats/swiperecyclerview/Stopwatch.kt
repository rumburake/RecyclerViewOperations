package com.threecats.swiperecyclerview

class Stopwatch {
    val laps = mutableListOf<Long>(System.nanoTime())

    fun lap(): Long {
        val now = System.nanoTime()
        val elapsed = now - laps.last()
        laps.add(now)
        return elapsed
    }

    fun lapUs() = lap() / 1000.0

    fun lapMs() = lap() / 1000 / 1000.0
}