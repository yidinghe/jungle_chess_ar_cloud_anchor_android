package com.ar.animal.chess.controller

import com.ar.animal.chess.model.Animal
import com.ar.animal.chess.model.GameState
import com.ar.animal.chess.model.Tile
import com.ar.animal.chess.storage.ChessStorageManager
import com.ar.animal.chess.util.d
import com.ar.animal.chess.util.e


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
    fun initGame(cloudAnchorId: String, initGameCallback: (roomId: String?) -> Unit) {
        //TODO added User A info to submit to network
        mStorageManager.nextRoomId { roomId ->
            if (roomId == null) {
                e(TAG, "Could not obtain a short code.")
                initGameCallback(null)
            } else {
                mRoomId = roomId
                mStorageManager.storeCloudAnchorIdUsingRoomId(roomId, cloudAnchorId)
                d(TAG, "Anchor hosted stored shortCode: $roomId" +
                        " CloudId: $cloudAnchorId")
                initGameCallback(roomId.toString())
            }
        }
    }

    fun initGameBoard(tileList: List<Tile>,
                      animalAList: List<Animal>, animalBList: List<Animal>) {
        //TODO
    }

    //USER B needs to pairGame with a valid roomId
    fun pairGame(roomId: Int, pairGameCallback: (cloudAnchorId: String?) -> Unit) {
        //TODO added UserB info to submit to network
        mStorageManager.getCloudAnchorId(roomId) { cloudAnchorId ->
            mRoomId = roomId
            if (cloudAnchorId == null) {
                e(TAG, "Could not obtain a cloudAnchorId.")
                pairGameCallback(null)
            } else {
                d(TAG, "Obtain cloudAnchorId success" +
                        " CloudId: $cloudAnchorId")
                pairGameCallback(cloudAnchorId)
            }
        }
    }

    fun updateGameInfo() {
        //TODO
    }

    fun storeUserInfo(){
        //TODO  store user info
    }
}