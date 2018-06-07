package com.ar.animal.chess.controller

import android.os.Handler
import com.ar.animal.chess.model.*
import com.ar.animal.chess.storage.ChessStorageManager
import com.ar.animal.chess.util.d
import com.ar.animal.chess.util.e


class ChessGameController {
    private val TAG = ChessGameController::class.java.simpleName
    private val mStorageManager = ChessStorageManager()
    private var mCurrentGameState = GameState.NO_WIN_USER
    private var mRoomId = 0
    private var mCurrentUser: ChessUserInfo? = null
    private var mOtherUser: ChessUserInfo? = null
    private var mIsGameStarted = false
    private var mCurrentRound = 0
    private var mAnimalList: List<Animal>? = null

    private val BASEMENT_A_X = 3
    private val BASEMENT_A_Y = 8
    private val BASEMENT_B_X = 3
    private val BASEMENT_B_Y = 0
    private val mHandler = Handler()

    private lateinit var onAnimalUpdate: (updatedAnimalList: List<Animal>) -> Unit
    private lateinit var onGameFinish: (gameState: GameState, currentRound: Int) -> Unit
    private lateinit var onGameGlobalInfoUpdate: (gameState: GameState, currentRound: Int) -> Unit

    companion object {
        val instance: ChessGameController = ChessGameController()
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
                    mStorageManager.readAnimalInfo(mRoomId) {
                        handleReceiveAnimalListUpdate(it)
                    }
                    mStorageManager.readGameGlobalInfo(mRoomId) { gameState, currentRound ->
                        handleReceiveGameGlobalInfoUpdate(gameState, currentRound)
                    }
                }
            }

            if (isUserAReady && isUserBReady) {
                d(TAG, "isUserAReady, isUserBReady, start game, now UserA turn")
                onGameStart(isUserAReady, isUserBReady)
            }
        }
    }

    private fun handleReceiveGameGlobalInfoUpdate(gameState: GameState, currentRound: Int) {
        d(TAG, "handleReceiveGameGlobalInfoUpdate")
        mCurrentRound = currentRound
        when (gameState) {
            GameState.USER_A_WIN_ATTACK_BASEMENT, GameState.USER_B_WIN_ATTACK_BASEMENT,
            GameState.USER_A_WIN_KILL_ALL, GameState.USER_B_WIN_KILL_ALL, GameState.NO_WIN_USER -> {
                d(TAG, "handleReceiveGameGlobalInfoUpdate, onGameFinish")
                onGameFinish(gameState, mCurrentRound)
            }
            GameState.USER_A_TURN, GameState.USER_B_TURN -> {
                d(TAG, "handleReceiveGameGlobalInfoUpdate, onGameGlobalInfoUpdate")
                onGameGlobalInfoUpdate(gameState, currentRound)
            }
        }
    }

    private fun handleReceiveAnimalListUpdate(updatedAnimalList: List<Animal>) {
        d(TAG, "handleReceiveAnimalListUpdate")
        if (updatedAnimalList.size != 16) {
            e(TAG, "animal List size is not 16, no need to convert")
            return
        }

        val needToNotifyUIList = updatedAnimalList.minus(mAnimalList!!)
        d(TAG, "needToNotifyUIList: $needToNotifyUIList")
        mAnimalList = updatedAnimalList

        if (needToNotifyUIList.isNotEmpty())
            onAnimalUpdate(needToNotifyUIList)
    }

    fun test() {
        mStorageManager.readGameGlobalInfo(7) { gameState, currentRound ->
            d(TAG, "gameState: $gameState, currentRound: $currentRound")
        }
    }

    /**
     *  Every User finish his turn, call updateGameInfo, other User will receive onUserTurn callback
     *  then UI needs to redraw and start another round
     */
    fun updateGameInfo(updatedAnimal1: Animal, updatedAnimal2: Animal?) {
        d(TAG, "updateGameInfo")

        val userType = mCurrentUser!!.userType

        if (userType == UserType.USER_A && updatedAnimal1.animalDrawType == AnimalDrawType.TYPE_B) {
            e(TAG, "USER_A, user type not match chess type, error.")
            return
        }

        if (userType == UserType.USER_B && updatedAnimal1.animalDrawType == AnimalDrawType.TYPE_A) {
            e(TAG, "USER_B, user type not match chess type, error.")
            return
        }

        mCurrentRound++
        if (mCurrentRound >= 100) {
            mCurrentGameState = GameState.NO_WIN_USER
            d(TAG, "current round: $mCurrentRound, NO_WIN_USER")
            mStorageManager.writeGameGlobalInfo(mRoomId, mCurrentGameState, mCurrentRound)
            return
        }

        if (checkBasementAttack(updatedAnimal1)) {
            d(TAG, "checkBasementAttack, game finish no need to store the db.")
            return
        }

        mAnimalList!!.toMutableList().remove(mAnimalList!!.find {
            it.animalDrawType == updatedAnimal1.animalDrawType
                    && it.animalType == updatedAnimal1.animalType
        })
        mAnimalList!!.plus(updatedAnimal1)
        updatedAnimal2?.let {
            mAnimalList!!.toMutableList().remove(mAnimalList!!.find {
                it.animalDrawType == updatedAnimal2.animalDrawType
                        && it.animalType == updatedAnimal2.animalType
            })
            mAnimalList!!.plus(it)
        }
        d(TAG, "mergeList: ${mAnimalList!!.size},  $mAnimalList")

        if (checkCurrentGameFinish()) {
            d(TAG, "checkCurrentGameFinish, game finish no need to store the db.")
            return
        }

        mStorageManager.writeAnimalInfo(mRoomId, mAnimalList!!)
        //TODO change User turn, now only change the turn between USER_A and USER_B
        if (mCurrentGameState == GameState.USER_A_TURN) {
            mCurrentGameState = GameState.USER_B_TURN
        } else if (mCurrentGameState == GameState.USER_B_TURN) {
            mCurrentGameState = GameState.USER_A_TURN
        }
        mHandler.postDelayed({ mStorageManager.writeGameGlobalInfo(mRoomId, mCurrentGameState, mCurrentRound) }, 5000)
    }

    fun initGameBoard(animalList: List<Animal>) {
        d(TAG, "initGameBoard")
        if (mCurrentUser == null) {
            e(TAG, "current user is null, no need to store the gameBoard")
            return
        }
        mAnimalList = animalList

        if (mCurrentUser!!.userType == UserType.USER_A) {
            d(TAG, "initGameBoard, userType UserA, store current game board do db.")
            mStorageManager.writeAnimalInfo(mRoomId, animalList)
        }

    }

    fun setOnAnimalUpdateListener(onAnimalUpdate: (updatedAnimalList: List<Animal>) -> Unit) {
        d(TAG, "setOnAnimalUpdateListener")
        this.onAnimalUpdate = onAnimalUpdate
    }

    fun setOnGameFinishListener(onGameFinish: (gameState: GameState, currentRound: Int) -> Unit) {
        d(TAG, "setOnGameFinishListener")
        this.onGameFinish = onGameFinish
    }

    fun setOnGameGlobalInfoUpdateListener(onGameGlobalInfoUpdate:
                                          (gameState: GameState, currentRound: Int) -> Unit) {
        d(TAG, "setOnGameGlobalInfoUpdateListener")
        this.onGameGlobalInfoUpdate = onGameGlobalInfoUpdate
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

    fun getUserInfo(isNeedUserA: Boolean, onReadUserInfo: (currentUserInfo: ChessUserInfo,
                                                           otherUserInfo: ChessUserInfo) -> Unit) {
        d(TAG, "getUserInfo: $isNeedUserA")
        if (mCurrentUser == null) {
            e(TAG, "getUserInfo, init current user First")
            return
        }

        mStorageManager.readUserInfo(mRoomId, isNeedUserA) { chessUserInfo ->
            d(TAG, "onReadUserInfo: $chessUserInfo")
            if (isNeedUserA && chessUserInfo.userType == UserType.USER_B || ((!isNeedUserA)
                            && chessUserInfo.userType == UserType.USER_A)) {
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

    private fun checkBasementAttack(updatedAnimal: Animal): Boolean {
        d(TAG, "checkBasementAttack: $updatedAnimal")

        with(updatedAnimal) {
            if (animalDrawType == AnimalDrawType.TYPE_A && posCol == BASEMENT_B_X && posRow == BASEMENT_B_Y) {
                d(TAG, "checkBasementAttack USER_A_WIN_ATTACK_BASEMENT")
                mCurrentGameState = GameState.USER_A_WIN_ATTACK_BASEMENT
                onGameFinish(GameState.USER_A_WIN_ATTACK_BASEMENT, mCurrentRound)
                mStorageManager.writeGameGlobalInfo(mRoomId, mCurrentGameState, mCurrentRound)
                return true
            } else if (animalDrawType == AnimalDrawType.TYPE_B && posCol == BASEMENT_A_X && posRow == BASEMENT_A_Y) {
                d(TAG, "checkBasementAttack USER_B_WIN_ATTACK_BASEMENT")
                mCurrentGameState = GameState.USER_B_WIN_ATTACK_BASEMENT
                onGameFinish(GameState.USER_B_WIN_ATTACK_BASEMENT, mCurrentRound)
                mStorageManager.writeGameGlobalInfo(mRoomId, mCurrentGameState, mCurrentRound)
                return true
            }
        }

        return false
    }

    private fun checkCurrentGameFinish(): Boolean {
        d(TAG, "checkCurrentGameFinish")

        var animalAliveCountA = 8
        var animalAliveCountB = 8

        mAnimalList!!.forEach {
            if (it.state == AnimalState.DEAD) {
                if (it.animalDrawType == AnimalDrawType.TYPE_A) {
                    animalAliveCountA--
                } else {
                    animalAliveCountB--
                }
            }
        }

        if (animalAliveCountA <= 0) {
            d(TAG, "checkCurrentGameFinish USER_B_WIN_KILL_ALL")
            mCurrentGameState = GameState.USER_B_WIN_KILL_ALL
            onGameFinish(GameState.USER_B_WIN_KILL_ALL, mCurrentRound)
            mStorageManager.writeGameGlobalInfo(mRoomId, mCurrentGameState, mCurrentRound)
            return true
        } else if (animalAliveCountB <= 0) {
            d(TAG, "checkCurrentGameFinish USER_A_WIN_KILL_ALL")
            mCurrentGameState = GameState.USER_A_WIN_KILL_ALL
            onGameFinish(GameState.USER_A_WIN_KILL_ALL, mCurrentRound)
            mStorageManager.writeGameGlobalInfo(mRoomId, mCurrentGameState, mCurrentRound)
            return true
        }
        return false
    }

}