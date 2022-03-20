package chess

import kotlin.math.abs
import kotlin.system.exitProcess

fun main() {
//    write your code here
    Board()
}
class Board(private val size: Int = 8) {

    private val colors = listOf("white", "black", "green")
    private val initialBlackRow = 1
    private val initialWhiteRow = 6
    private val blackPawns = MutableList<Pair<Int, Int>>(size) { index -> Pair(initialBlackRow, index) }
    private val whitePawns = MutableList<Pair<Int, Int>>(size) { index -> Pair(initialWhiteRow, index) }
    private var lastMoveValidForEnPassant: Pair<Int, Int>? = null


    private val players = mutableListOf<String>()
    private var winner:Int = -1
    private var whoseTurn:Int = 0

    //<editor-fold desc="MESSAGES">
    private val INVALID_INPUT = "Invalid Input"
    private val EXIT = "exit"
    private val EXIT_MESSAGE = "Bye!"
    //</editor-fold>

    //<editor-fold desc="MOVES">
    private val blackMove = 1
    private val whiteMove = -1
    //</editor-fold>

    init {
        introduceGame()
        setPlayers()
        drawBoard()
        play()
    }

    private fun introduceGame(){
        val gameTitle = "Pawns-Only Chess"
        println(gameTitle)
    }

    private fun setPlayers(numberOfPlayers: Int = 2) {
        players.clear()
        List(numberOfPlayers) { index ->
            print(
                when (index) {
                    0 -> "First"
                    1 -> "Second"
                    else -> "Other"
                }
            )
            println(" Player's name:")
            players += readln()
        }
    }

    private fun play() {
        while (winner == -1) {
            callForTurn()
            checkWinner()
            ++whoseTurn
        }
    }


    private fun callForTurn() : String{
        whoseTurn %= players.size
        var input: String
        do {
            println("${players[whoseTurn]}'s turn:")
            input = readln()
        } while (!canMove(input))
        return input
    }



    private fun isTopRankedWinner() =
        getPlayerPawns().any {pair -> pair.first == getTopRankRow()}
    private fun isOponentTopRankedWinner() =
        getOpenentPawns().any {pair -> pair.first == getOponentTopRankRow()}

    private fun isAllOponentPawnsCapturedWinner() = getOpenentPawns().isEmpty()
    private fun isAllPlayerPawnsCapturedWinner() = getPlayerPawns().isEmpty()
    private fun isAllOponentPawnsStaledWinner()  =
        getOpenentPawns().all { pawn ->
            hasAnOponentAhead(pawn, getOponentTurn()) && !canCapture(pawn, getOponentTurn())
        }

    private fun hasAnOponentAhead(pawn: Pair<Int, Int>, player: Int = whoseTurn) =
        getOpenentPawns(player).any { oponent ->
            pawn.first + getDirectionality(player) == oponent.first && pawn.second == oponent.second
        }
    private fun canCapture(pawn: Pair<Int, Int>, player: Int = whoseTurn) =
        getOpenentPawns(player).any { oponent ->
            pawn.first + getDirectionality(player) == oponent.first &&
                    abs(pawn.second - oponent.second) == 1
        }

    private fun getPlayerTurn() = whoseTurn
    private fun getOponentTurn() = whoseTurn + 1 % players.size

    private fun checkWinner() {
        winner = if (isTopRankedWinner() || isAllOponentPawnsCapturedWinner()) {
            getPlayerTurn()
        } else if (isOponentTopRankedWinner() || isAllPlayerPawnsCapturedWinner()) {
            getOponentTurn()
        } else {
            -1
        }
        if (winner > -1) {
            println(
                "${
                    when (winner) {
                        0 -> "White"
                        1 -> "Black"
                        else -> "OTHER"
                    }
                } Wins!"
            )
            println("Bye!")
        }
        if (isAllOponentPawnsStaledWinner()) {
            println("Stalemate!")
            println("Bye!")
            winner = getPlayerTurn()
        }
    }

    private fun getPlayerPawns(player: Int = whoseTurn)= if(player == 0) whitePawns else blackPawns
    private fun getOpenentPawns(player: Int = whoseTurn) = if (player == 0) blackPawns else whitePawns

    private fun getInitialRow(player: Int = whoseTurn) = if (player == 0) initialWhiteRow else initialBlackRow

    private fun getTopRankRow(player: Int = whoseTurn) = if (player == 0) 0 else size - 1
    private fun getOponentTopRankRow(player: Int = whoseTurn) = if (player == 0) size - 1 else 0

    private fun getDirectionality(player: Int = whoseTurn) = if (player == 0) whiteMove else blackMove

    private fun isPawnExist(move: Move) = getPlayerPawns().indexOf(move.from) > -1

    private fun isForwardMove(move: Move) = move.isMovingForward(getInitialRow(), getDirectionality())
    private fun isCaptureMove(move: Move) = move.isMovingDiagonally() && getOpenentPawns().contains(move.to)
    private fun isEnPassantCaptureMove(move: Move) = move.isMovingDiagonally(lastMoveValidForEnPassant)

    private fun isTargetAvailable(move: Move): Boolean {
        val taken = whitePawns.contains(move.to) || blackPawns.contains(move.to)
        return !taken
    }

    private fun canMove(action: String) : Boolean{
        if (action == EXIT) {
            println(EXIT_MESSAGE)
            exitProcess(0)
        }

        var validMove = false

        val move = Move(this, action)

        if (!move.isWithinBoundaries()) {
            println(INVALID_INPUT)
        } else if (!isPawnExist(move)) {
            println("No ${colors[whoseTurn]} pawn at ${move.fromPosition}")
        } else if (isForwardMove(move)) {
            if (isTargetAvailable(move)) {
                validMove = true
            } else {
                println(INVALID_INPUT)
            }
        } else if (isCaptureMove(move)){
            validMove = true
            val oponent = if (whoseTurn == 0) blackPawns else whitePawns
            oponent.remove(move.to)
        } else if (isEnPassantCaptureMove(move)){
            if (isTargetAvailable(move)) {
                validMove = true
                val oponent = if (whoseTurn == 0) blackPawns else whitePawns
                oponent.remove(lastMoveValidForEnPassant)
            } else {
                println(INVALID_INPUT)
            }
        } else {
            println(INVALID_INPUT)
        }

        if (validMove) {
            move(move)
            lastMoveValidForEnPassant = if (move.isInitialMoveForPlayer(whoseTurn)) move.to else null
        }
        return validMove
    }


    private fun move(move: Move) {
        val playerPawns = getPlayerPawns()
        val index = playerPawns.indexOf(move.from)
        playerPawns[index] = move.to
        drawBoard()
    }


    class Position(private val data: String, private val boardSize: Int) {
        private val column = data.first { it.isLetter()} - 'a'
        private val row = boardSize - (data.first { it.isDigit()}).digitToInt()

        fun toPair(): Pair<Int, Int> {
            return Pair(row, column)
        }
        fun fromPair(pair: Pair<Int, Int>, board: Board): Position {
            return Position("${'a' + pair.second}${board.size - pair.first}", board.size)
        }
        override fun toString(): String {
            return data
        }
    }


    class Move(private val board: Board, private val move: String) {
        private val forwardMove = 1
        private val forwardMoveInitial = listOf(1, 2)

        private val lettersRange = "[a-${'a' + board.size - 1}]"
        private val digitsRange = "[1-${board.size}]"
        private val positionPattern = "($lettersRange$digitsRange)"

        val fromPosition = move.substring(0, 2)
        val from = Position(fromPosition, board.size).toPair()

        val toPosition = move.substring(2)
        val to =  Position(toPosition, board.size).toPair()

        fun isMovingVertically() = from.second == to.second

        fun isMovingDiagonally() = abs(from.second - to.second) == 1 && abs(from.first - to.first) == 1
        fun isMovingDiagonally(withEnPassant: Pair<Int, Int>?) = withEnPassant != null &&
                isMovingDiagonally() &&
                from.first == withEnPassant.first && abs(from.second - withEnPassant.second) == 1 &&
                to.second == withEnPassant.second && abs(to.first - withEnPassant.first) == 1


        fun isInitialMove(initialRow: Int) = from.first == initialRow
        fun isInitialMoveForPlayer(player: Int) = from.first ==  if (player == 0) board.initialWhiteRow else board.initialBlackRow

        val verticalSteps = to.first - from.first
        val horizontalSteps = to.second - from.second

        fun isMovingForward(initialRow: Int, directionality: Int) = isMovingVertically() &&
            (
                (isInitialMove(initialRow) && verticalSteps in forwardMoveInitial.map { it * directionality }) ||
                    (verticalSteps == forwardMove * directionality)
            )
        fun isWithinBoundaries(): Boolean {
            val pattern = Regex(positionPattern.repeat(2))
            return move.lowercase().matches(pattern)
        }

        override fun toString(): String {
            return move
        }
    }

    //<editor-fold desc="DRAWING THE BOARD">
    private fun drawBoard(){
        List(size){
            drawRow(it)
        }
        drawFooter()
    }

    private fun drawFooter(){
        drawLine()
        val a = 'a'

        val footerHeader = "  "
        print(footerHeader)

        List(size) {
            print("  ${a+it} ")
        }
        println()
    }

    private fun drawRow(rowNumber: Int){
        drawLine()

        val rowHeader = "${size - rowNumber} |"
        print(rowHeader)

        List(size) {
            drawCell(rowNumber, it)
        }
        println()
    }

    private fun drawLine(){
        val lineHeader = "  +"
        val lineCell = "---+"
        val line = "$lineHeader${lineCell.repeat(size)}"
        println(line)
    }

    private fun drawCell(rowNumber: Int, columnNumber: Int){
        //        val pawn = pawns[rowNumber][columnNumber]
        val pawn = if (blackPawns.contains(Pair(rowNumber, columnNumber))) {
            'B'
        } else if (whitePawns.contains(Pair(rowNumber, columnNumber))) {
            'W'
        } else {
            ' '
        }
        print(" $pawn |")
    }
    //</editor-fold>
}

