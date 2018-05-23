package com.ar.animal.chess.controller

import com.ar.animal.chess.model.AnimalTileDbModel
import com.ar.animal.chess.model.GameState
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

    fun initGame(isUserA: Boolean, cloudAnchorId: String) {
        //TODO
        mGameState = if (isUserA) GameState.USER_A_TURN else GameState.USER_B_TURN
    }

    fun updateGameInfo(animalTileList: List<AnimalTileDbModel>) {
        //TODO
    }
}