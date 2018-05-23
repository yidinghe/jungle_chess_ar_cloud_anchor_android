package com.ar.animal.chess.controller

import com.ar.animal.chess.model.Animal
import com.ar.animal.chess.model.GameState
import com.ar.animal.chess.model.Tile
import com.ar.animal.chess.storage.ChessStorageManager


class GameController {
    private val TAG = GameController::class.java.simpleName
    private val mStorageManager = ChessStorageManager()
    private var mGameState = GameState.NO_WIN_USER
    private var mCloudAnchorId: String = ""
    private var mRoomId = 0

    companion object {
        val instance: GameController = GameController()
    }

    //FOR init game, User A needs to store
    fun initGame(isUserA: Boolean, cloudAnchorId: String, tileList: List<Tile>,
                 animalAList: List<Animal>, animalBList: List<Animal>) {
        //TODO
        mGameState = if (isUserA) GameState.USER_A_TURN else GameState.USER_B_TURN
    }

    fun updateGameInfo() {
        //TODO
    }
}