package com.example.game

/**
 * Nine Men's Morris (Lusalos) Board Topology.
 * 24 Intersections across 3 concentric squares:
 *
 * Outer Square (0..7):
 *  0 (Top-Left),   1 (Top-Mid),    2 (Top-Right)
 *  7 (Mid-Left),                   3 (Mid-Right)
 *  6 (Bottom-Left), 5 (Bottom-Mid), 4 (Bottom-Right)
 *
 * Middle Square (8..15):
 *  8 (TL),   9 (TM),  10 (TR)
 * 15 (ML),            11 (MR)
 * 14 (BL),  13 (BM),  12 (BR)
 *
 * Inner Square (16..23):
 * 16 (TL),  17 (TM),  18 (TR)
 * 23 (ML),            19 (MR)
 * 22 (BL),  21 (BM),  20 (BR)
 */
object BoardConstants {
    const val TOTAL_POINTS = 24
    const val PIECES_PER_PLAYER = 9

    // Coordinates normalized from 0f..1f for canvas rendering (x, y)
    val POINT_COORDINATES = arrayOf(
        // Outer square (0..7)
        0.08f to 0.08f, // 0: TL
        0.50f to 0.08f, // 1: TM
        0.92f to 0.08f, // 2: TR
        0.92f to 0.50f, // 3: MR
        0.92f to 0.92f, // 4: BR
        0.50f to 0.92f, // 5: BM
        0.08f to 0.92f, // 6: BL
        0.08f to 0.50f, // 7: ML

        // Middle square (8..15)
        0.22f to 0.22f, // 8: TL
        0.50f to 0.22f, // 9: TM
        0.78f to 0.22f, // 10: TR
        0.78f to 0.50f, // 11: MR
        0.78f to 0.78f, // 12: BR
        0.50f to 0.78f, // 13: BM
        0.22f to 0.78f, // 14: BL
        0.22f to 0.50f, // 15: ML

        // Inner square (16..23)
        0.36f to 0.36f, // 16: TL
        0.50f to 0.36f, // 17: TM
        0.64f to 0.36f, // 18: TR
        0.64f to 0.50f, // 19: MR
        0.64f to 0.64f, // 20: BR
        0.50f to 0.64f, // 21: BM
        0.36f to 0.64f, // 22: BL
        0.36f to 0.50f  // 23: ML
    )

    // Adjacency graph for moving pieces along valid lines
    val ADJACENT_POINTS: Array<IntArray> = arrayOf(
        /* 0  */ intArrayOf(1, 7),
        /* 1  */ intArrayOf(0, 2, 9),
        /* 2  */ intArrayOf(1, 3),
        /* 3  */ intArrayOf(2, 4, 11),
        /* 4  */ intArrayOf(3, 5),
        /* 5  */ intArrayOf(4, 6, 13),
        /* 6  */ intArrayOf(5, 7),
        /* 7  */ intArrayOf(6, 0, 15),

        /* 8  */ intArrayOf(9, 15),
        /* 9  */ intArrayOf(8, 10, 1, 17),
        /* 10 */ intArrayOf(9, 11),
        /* 11 */ intArrayOf(10, 12, 3, 19),
        /* 12 */ intArrayOf(11, 13),
        /* 13 */ intArrayOf(12, 14, 5, 21),
        /* 14 */ intArrayOf(13, 15),
        /* 15 */ intArrayOf(14, 8, 7, 23),

        /* 16 */ intArrayOf(17, 23),
        /* 17 */ intArrayOf(16, 18, 9),
        /* 18 */ intArrayOf(17, 19),
        /* 19 */ intArrayOf(18, 20, 11),
        /* 20 */ intArrayOf(19, 21),
        /* 21 */ intArrayOf(20, 22, 13),
        /* 22 */ intArrayOf(21, 23),
        /* 23 */ intArrayOf(22, 16, 15)
    )

    // All 16 valid mills (lines of three points)
    val MILLS: Array<IntArray> = arrayOf(
        // Outer square (4)
        intArrayOf(0, 1, 2),
        intArrayOf(2, 3, 4),
        intArrayOf(4, 5, 6),
        intArrayOf(6, 7, 0),

        // Middle square (4)
        intArrayOf(8, 9, 10),
        intArrayOf(10, 11, 12),
        intArrayOf(12, 13, 14),
        intArrayOf(14, 15, 8),

        // Inner square (4)
        intArrayOf(16, 17, 18),
        intArrayOf(18, 19, 20),
        intArrayOf(20, 21, 22),
        intArrayOf(22, 23, 16),

        // Cross connections (4)
        intArrayOf(1, 9, 17),
        intArrayOf(3, 11, 19),
        intArrayOf(5, 13, 21),
        intArrayOf(7, 15, 23)
    )

    // Quick lookup: list of mill indices that point `index` belongs to
    val POINT_MILLS: Array<IntArray> = Array(TOTAL_POINTS) { pt ->
        val list = mutableListOf<Int>()
        MILLS.forEachIndexed { millIdx, mill ->
            if (mill.contains(pt)) list.add(millIdx)
        }
        list.toIntArray()
    }

    // Board line segments for rendering
    val BOARD_LINES = listOf(
        // Outer square
        0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 0,
        // Middle square
        8 to 9, 9 to 10, 10 to 11, 11 to 12, 12 to 13, 13 to 14, 14 to 15, 15 to 8,
        // Inner square
        16 to 17, 17 to 18, 18 to 19, 19 to 20, 20 to 21, 21 to 22, 22 to 23, 23 to 16,
        // Cross connections
        1 to 9, 9 to 17,
        3 to 11, 11 to 19,
        5 to 13, 13 to 21,
        7 to 15, 15 to 23
    )
}
