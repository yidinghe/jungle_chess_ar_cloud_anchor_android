package com.ar.animal.chess.controller

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

    private lateinit var onAnimalUpdate: (updatedAnimalList: List<Animal>) -> Unit
    private lateinit var onGameFinish: (gameState: GameState, currentRound: Int) -> Unit

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
                    mStorageManager.readAnimalInfo(mRoomId) {
                        handleReceiveAnimalListUpdate(it)
                    }
                }
            }

            if (isUserAReady && isUserBReady) {
                d(TAG, "isUserAReady, isUserBReady, start game, now UserA turn")
                onGameStart(isUserAReady, isUserBReady)
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

        onAnimalUpdate(needToNotifyUIList)
    }

    fun test() {
        val animalList = mutableListOf<Animal>(
                Animal(0, 8, AnimalState.ALIVE, AnimalType.RAT, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.CAT, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.DOG, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.WOLF, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.LEOPARD, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.TIGER, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.LION, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.ELEPHANT, AnimalDrawType.TYPE_A),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.RAT, AnimalDrawType.TYPE_B),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.CAT, AnimalDrawType.TYPE_B),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.DOG, AnimalDrawType.TYPE_B),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.WOLF, AnimalDrawType.TYPE_B),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.LEOPARD, AnimalDrawType.TYPE_B),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.TIGER, AnimalDrawType.TYPE_B),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.LION, AnimalDrawType.TYPE_B),
                Animal(0, 8, AnimalState.ALIVE, AnimalType.ELEPHANT, AnimalDrawType.TYPE_B)
        )

        val animal1 = Animal(1, 8, AnimalState.ALIVE, AnimalType.RAT, AnimalDrawType.TYPE_A)

        val animal2 = Animal(1, 8, AnimalState.ALIVE, AnimalType.WOLF, AnimalDrawType.TYPE_A)

        animalList.remove(animalList.find { it.animalDrawType == animal1.animalDrawType && it.animalType == animal1.animalType })

        d(TAG, "mergeList: ${animalList.size}")
        animalList.remove(animalList.find { it.animalDrawType == animal2.animalDrawType && it.animalType == animal2.animalType })
        d(TAG, "mergeList2: ${animalList.size}")
        val mergeList = animalList.plus(animal1).plus(animal2)
        d(TAG, "mergeList: ${mergeList.size},  $mergeList")
    }

    /**
     *  Every User finish his turn, call updateGameInfo, other User will receive onUserTurn callback
     *  then UI needs to redraw and start another round
     */
    fun updateGameInfo(updatedAnimal1: Animal, updatedAnimal2: Animal?) {
        //TODO
        d(TAG, "updateGameInfo")
        mCurrentRound++

        mAnimalList!!.toMutableList().remove(mAnimalList!!.find { it.animalDrawType == updatedAnimal1.animalDrawType && it.animalType == updatedAnimal1.animalType })
        mAnimalList!!.plus(updatedAnimal1)
        updatedAnimal2?.let {
            mAnimalList!!.toMutableList().remove(mAnimalList!!.find { it.animalDrawType == updatedAnimal2.animalDrawType && it.animalType == updatedAnimal2.animalType })
            mAnimalList!!.plus(it)
        }
        d(TAG, "mergeList: ${mAnimalList!!.size},  $mAnimalList")

        mStorageManager.writeAnimalInfo(mRoomId, mAnimalList!!)
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