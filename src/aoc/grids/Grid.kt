package aoc.grids

open class Grid(input: List<String>) {
    val height = input.size
    val width = input.maxOf { it.length }
    private val data = input.mapIndexed { y, line -> line.mapIndexed { x, char -> Cell(x, y, char.toString()) }.toMutableList() }
    var wrap = false
    var borderValue = ""

    fun getCell(point: Point): Cell {
        return getCell(point.x, point.y)
    }

    fun getCell(x: Int, y: Int): Cell {
        val realX = if (wrap) (x + width) % width else x // Doesn't work if we're *WAY* out of bounds
        val realY = if (wrap) (y + height) % height else y
        return if (inBounds(realX, realY)) {
            data[realY][realX]
        } else {
            Cell(realX, realY, borderValue)
        }
    }

    fun setValue(point: Point, value: String) {
        setValue(point.x, point.y, value)
    }

    fun setValue(x: Int, y: Int, value: String) {
        val realX = if (wrap) (x + width) % width else x // Doesn't work if we're *WAY* out of bounds
        val realY = if (wrap) (y + height) % height else y
        if (inBounds(realX, realY)) {
            data[realY][realX] = Cell(realX, realY, value)
        }
    }

    fun cells(): List<Cell> {
        val cells = mutableListOf<Cell>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                cells.add(getCell(x, y))
            }
        }
        return cells.toList()
    }

    fun getNeighbors(point: Point, diagonal: Boolean = true, includeOob: Boolean = false): List<Cell> {
        val neighbors = mutableListOf<Cell>()
        for (dx in -1..1) {
            for (dy in -1 .. 1) {
                if (dx == 0 && dy == 0) continue
                if (!diagonal && dx != 0 && dy != 0) continue
                if (includeOob || wrap || inBounds(point.x + dx, point.y + dy)) {
                    neighbors.add(getCell(point.x + dx, point.y + dy))
                }
            }
        }
        return neighbors
    }

    fun fuzzySelect(point: Point, diagonal: Boolean = true): List<Cell> {
        val cell = getCell(point)
        return fuzzySelect(point, diagonal) { it.value == cell.value }
    }

    fun fuzzySelect(point: Point, diagonal: Boolean = true, match: (Cell) -> Boolean): List<Cell> {
        val start = getCell(point)
        val selected = mutableSetOf(start)
        val toAssess = mutableListOf(start)
        while(toAssess.isNotEmpty()) {
            val next = toAssess.removeLast()
            for (neighbor in getNeighbors(next, diagonal)) {
                if (selected.contains(neighbor) || !match(neighbor)) {
                    continue
                }
                selected.add(neighbor)
                toAssess.add(neighbor)
            }
        }
        return selected.toList()
    }

    fun getRegion(xRange: IntRange, yRange: IntRange): Region {
        val regionStrings = data.subList(yRange.first, yRange.last + 1).map { rowString(it).substring(xRange) }
        return Region(xRange.first, yRange.first, regionStrings)
    }

    fun findRegions(regex: Regex): List<Region> {
        val regions = mutableListOf<Region>()
        for ((i, row) in data.map { rowString(it) }.withIndex()) {
            for (match in regex.findAll(row)) {
                regions.add(getRegion(match.range, i..i))
            }
        }
        return regions
    }


    private fun inBounds(point: Point): Boolean {
        return inBounds(point.x, point.y)
    }

    private fun inBounds(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

    private fun rowString(row: List<Cell>): String {
        return row.fold("") { acc, v -> acc + v.value }
    }

    fun gridString(): String {
        return data.map { rowString(it) }.reduce { acc, v -> acc + "\n" + v }
    }

    override fun toString(): String {
        val header = "Grid(height=$height, width=$width, wrap=$wrap, borderValue='$borderValue')\n"
        return header + gridString()
    }
}