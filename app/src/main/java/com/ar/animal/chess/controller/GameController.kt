package com.ar.animal.chess.controller

import com.ar.animal.chess.model.*
import com.ar.animal.chess.storage.ChessStorageManager
import com.ar.animal.chess.util.d
import com.ar.animal.chess.util.e


class GameController {
    private val TAG = GameController::class.java.simpleName
    private val mStorageManager = ChessStorageManager()
    private var mCurrentGameState = GameState.NO_WIN_USER
    private var mRoomId = 0
    private var mCurrentUser: ChessUserInfo? = null
    private var mOtherUser: ChessUserInfo? = null
    private var mIsGameStarted = false
    private var mCurrentRound = 0

    private lateinit var onAnimalUpdate: (updatedAnimalA: Animal, updatedAnimalB: Animal?) -> Unit
    private lateinit var onGameFinish: (gameState: GameState, currentRound: Int) -> Unit

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

    //USER B needs to pairGame with a valid roomId
    fun pairGame(roomId: Int, onPairGame: (cloudAnchorId: String?) -> Unit) {
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

    /**
     *  Both UserA and UserB confirm GameStart then UI will receive the onGameStart callback
     *  Start UserA round first
     */
    fun confirmGameStart(onGameStart: (isUserAConfirm: Boolean, isUserBConfirm: Boolean) -> Unit) {
        d(TAG, "confirmGameStart")
        //TODO
        if (mCurrentUser == null) {
            return
        }

        val isCurrentUserA = mCurrentUser!!.userType == UserType.USER_A

        mStorageManager.writeGameStart(mRoomId, isCurrentUserA)
        mStorageManager.readGameStart(mRoomId) { isUserAReady, isUserBReady ->
            d(TAG, "confirmGameStart: isUserAReady: $isUserAReady, isUserBReady: $isUserBReady")

            if ((isCurrentUserA && isUserBReady) || (!isCurrentUserA && isUserAReady)) {
                if (!mIsGameStarted) {
                    d(TAG, "GameStart, mark current game state to USER_A_TURN, start to listen animal update")
                    mCurrentGameState = GameState.USER_A_TURN
                    mIsGameStarted = true
                    //TODO read animal info change and return to UI
                }
            }

            if (isUserAReady && isUserBReady) {
                d(TAG, "isUserAReady, isUserBReady, start game, now UserA turn")
                onGameStart(isUserAReady, isUserBReady)
            }
        }
    }

    fun test() {
        mStorageManager.readGameStart(11) { isUserAReady, isUserBReady ->
            d(TAG, "confirmGameStart: isUserAReady: $isUserAReady, isUserBReady: $isUserBReady")
        }
    }

    /**
     *  Every User finish his turn, call updateGameInfo, other User will receive onUserTurn callback
     *  then UI needs to redraw and start another round
     */
    fun updateGameInfo(updatedAnimal1: Animal, updatedAnimal2: Animal?) {
        //TODO
        d(TAG, "updateGameInfo")
        mCurrentRound++
        when (mCurrentGameState) {

            GameState.USER_A_TURN -> TODO()
            GameState.USER_B_TURN -> TODO()
            GameState.USER_A_WIN -> TODO()
            GameState.USER_B_WIN -> TODO()
            GameState.NO_WIN_USER -> TODO()
        }

    }

    fun initGameBoard(animalList: List<Animal>) {
        //TODO
    }

    fun setOnAnimalUpdateListener(onAnimalUpdate: (updatedAnimalA: Animal, updatedAnimalB: Animal?) -> Unit) {
        d(TAG, "setOnAnimalUpdateListener")
        this.onAnimalUpdate = onAnimalUpdate
    }

    fun setOnGameFinishListener(onGameFinish: (gameState: GameState, currentRound: Int) -> Unit) {
        d(TAG, "setOnGameFinishListener")
        this.onGameFinish = onGameFinish
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

    fun getUserInfo(isNeedUserA: Boolean, onReadUserInfo: (currentUserInfo: ChessUserInfo, otherUserInfo: ChessUserInfo) -> Unit) {
        d(TAG, "getUserInfo: $isNeedUserA")
        if (mCurrentUser == null) {
            e(TAG, "getUserInfo, init current user First")
            return
        }

        mStorageManager.readUserInfo(mRoomId, isNeedUserA) { chessUserInfo ->
            d(TAG, "onReadUserInfo: $chessUserInfo")
            if (isNeedUserA && chessUserInfo.userType == UserType.USER_B || ((!isNeedUserA) && chessUserInfo.userType == UserType.USER_A)) {
                e(TAG, "onReadUserInfo data is not needed, no need to notify UI")
            } else {
                mOtherUser = chessUserInfo
                if (mCurrentUser != null && mOtherUser != null)
                    onReadUserInfo(mCurrentUser!!, mOtherUser!!)
                else
                    e(TAG, "currentUser is null or otherUser is null")
            }
        }
    }
}