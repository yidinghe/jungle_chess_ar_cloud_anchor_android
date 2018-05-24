package com.ar.animal.chess.controller

import com.ar.animal.chess.model.*
import com.ar.animal.chess.storage.ChessStorageManager
import com.ar.animal.chess.util.d
import com.ar.animal.chess.util.e


class GameController {
    private val TAG = GameController::class.java.simpleName
    private val mStorageManager = ChessStorageManager()
    private var mGameState = GameState.NO_WIN_USER
    private var mCloudAnchorId: String = ""
    private var mRoomId = 0
    private var mCurrentUser: ChessUserInfo? = null
    private var mOtherUser: ChessUserInfo? = null

    companion object {
        val instance: GameController = GameController()
    }

    //FOR init game, User A needs to store
    fun initGame(cloudAnchorId: String, onInitGame: (roomId: String?) -> Unit) {

        mStorageManager.nextRoomId { roomId ->
            if (roomId == null) {
                e(TAG, "Could not obtain a short code.")
                onInitGame(null)
            } else {
                mRoomId = roomId
                mStorageManager.writeCloudAnchorIdUsingRoomId(roomId, cloudAnchorId)
                d(TAG, "Anchor hosted stored shortCode: $roomId" +
                        " CloudId: $cloudAnchorId")
                onInitGame(roomId.toString())
            }
        }
    }

    fun initGameBoard(tileList: List<Tile>,
                      animalAList: List<Animal>, animalBList: List<Animal>) {
        //TODO
    }

    //USER B needs to pairGame with a valid roomId
    fun pairGame(roomId: Int, onPairGame: (cloudAnchorId: String?) -> Unit) {
        //TODO added UserB info to submit to network
        mStorageManager.readCloudAnchorId(roomId) { cloudAnchorId ->
            mRoomId = roomId
            if (cloudAnchorId == null) {
                e(TAG, "Could not obtain a cloudAnchorId.")
                onPairGame(null)
            } else {
                d(TAG, "Obtain cloudAnchorId success" +
                        " CloudId: $cloudAnchorId")
                onPairGame(cloudAnchorId)
            }
        }
    }

    fun updateGameInfo() {
        //TODO
    }

    fun storeUserInfo(isUserA: Boolean, uid: String, displayName: String?, photoUrl: String?) {
        d(TAG, "storeUserInfo")
        val userInfo = ChessUserInfo(uid)
        displayName?.let {
            userInfo.displayName = it
        }
        photoUrl?.let {
            userInfo.photoUrl = it
        }
        userInfo.userType = if (isUserA) UserType.USER_A else UserType.USER_B
        mCurrentUser = userInfo
        mStorageManager.writeUserInfo(mRoomId, userInfo)
    }

    fun getUserInfo(onReadUserInfo: (currentUserInfo: ChessUserInfo, otherUserInfo: ChessUserInfo) -> Unit) {
        d(TAG, "getUserInfo")
        if (mCurrentUser == null) {
            e(TAG, "getUserInfo, init current user First")
            return
        }

        val isNeedGetUserA = mCurrentUser!!.userType != UserType.USER_A

        mStorageManager.readUserInfo(mRoomId, isNeedGetUserA) {
            if (isNeedGetUserA && it.userType == UserType.USER_A || (!isNeedGetUserA && it.userType == UserType.USER_B)) {
                e(TAG, "onReadUserInfo data is not needed, no need to notify UI")
            }else {
                mOtherUser = it
                if (mCurrentUser != null && mOtherUser != null)
                    onReadUserInfo(mCurrentUser!!, mOtherUser!!)
            }

        }
    }
}