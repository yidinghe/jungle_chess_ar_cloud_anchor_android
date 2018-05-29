package com.ar.animal.chess.storage

import com.ar.animal.chess.model.*
import com.ar.animal.chess.util.ChessConstants
import com.ar.animal.chess.util.d
import com.ar.animal.chess.util.e

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

/**
 * Helper class for Firebase storage of cloud anchor IDs.
 */

internal class ChessStorageManager {
    private val rootRef: DatabaseReference
    private val mAnimalEventListenerA = AnimalInfoUpdateListener()
    private val mAnimalEventListenerB = AnimalInfoUpdateListener()

    init {
        val rootDir = KEY_ROOT_DIR + ChessConstants.END_POINT.name
        rootRef = FirebaseDatabase.getInstance().reference.child(rootDir)
        DatabaseReference.goOnline()
    }

    /**
     * Gets a new short code that can be used to store the anchor ID.
     */
    fun nextRoomId(onReadroomId: (roomId: Int?) -> Unit) {
        d(TAG, "nextRoomId")
        // Run a transaction on the node containing the next short code available. This increments the
        // value in the database and retrieves it in one atomic all-or-nothing operation.
//
//        rootRef.child(KEY_NEXT_ROOM_ID).setValue(1)
//        onReadroomId(1)

        rootRef
                .child(KEY_NEXT_ROOM_ID)
                .runTransaction(
                        object : Transaction.Handler {

                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                var shortCode = currentData.getValue(Int::class.java)
                                if (shortCode == null) {
                                    shortCode = INITIAL_ROOM_ID - 1
                                }
                                currentData.value = shortCode + 1
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(
                                    error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                if (!committed) {
                                    e(TAG, "Firebase Error ${error?.toException()}")
                                    onReadroomId(null)
                                } else {
                                    if (currentData?.value == null) {
                                        onReadroomId(null)
                                    } else {
                                        val roomId = currentData.value as Long
                                        onReadroomId(roomId.toInt())
                                    }

                                }
                            }
                        })
    }

    /**
     * Stores the cloud anchor ID in the configured Firebase Database.
     */
    fun writeCloudAnchorIdUsingRoomId(shortCode: Int, cloudAnchorId: String) {
        d(TAG, "writeCloudAnchorIdUsingRoomId")
        val cloudAnchorDbModel = CloudAnchorDbModel(shortCode, cloudAnchorId, System.currentTimeMillis().toString())
        rootRef.child(shortCode.toString()).child(KEY_CONFIG).child(KEY_CLOUD_ANCHOR_CONFIG).setValue(cloudAnchorDbModel)
    }

    /**
     * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
     * was not stored for this short code.
     */
    fun readCloudAnchorId(shortCode: Int, onReadCloudAnchorId: (cloudAnchorId: String?) -> Unit) {
        d(TAG, "readCloudAnchorId: $shortCode")
        rootRef
                .child(shortCode.toString())
                .child(KEY_CONFIG)
                .child(KEY_CLOUD_ANCHOR_CONFIG)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                d(TAG, "readCloudAnchorId onDataChange")
                                val cloudAnchorDbModel = dataSnapshot.getValue(CloudAnchorDbModel::class.java)
                                if (cloudAnchorDbModel != null)
                                    onReadCloudAnchorId(cloudAnchorDbModel.cloudAnchorId)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                e(TAG, "The database operation for readCloudAnchorId was cancelled. ${error.toException()}")
                                onReadCloudAnchorId(null)
                            }
                        })
    }

    // this function for UserA listen UserB online event to start Game, and UserB grab UserA info to show the board

    fun readUserInfo(roomId: Int, isNeedGetUserA: Boolean, onReadUserInfo: (userInfo: ChessUserInfo) -> Unit) {
        val userRoot = if (isNeedGetUserA) KEY_USER_A else KEY_USER_B
        d(TAG, "readUserInfo: $userRoot")

        rootRef
                .child(roomId.toString())
                .child(KEY_CONFIG)
                .child(userRoot)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        d(TAG, "readUserInfo onCancelled")
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        d(TAG, "readUserInfo onDataChange")
                        val userDbModel = dataSnapshot.getValue(UserDbModel::class.java)
                        if (userDbModel != null) {
                            with(userDbModel) {
                                val userInfo = ChessUserInfo(userId, userName, userImageUrl)
                                userInfo.userType = if (userType == 0) UserType.USER_A else UserType.USER_B
                                onReadUserInfo(userInfo)
                            }

                        }
                    }

                })
    }

    fun writeUserInfo(roomId: Int, userInfo: ChessUserInfo) {
        val userRoot = if (userInfo.userType == UserType.USER_A) KEY_USER_A else KEY_USER_B
        d(TAG, "writeUserInfo, userRoot: $userRoot roomId: $roomId, userInfo: $userInfo")

        with(userInfo) {
            val userDbModel = UserDbModel(uid, userType.ordinal, photoUrl, displayName)
            rootRef.child(roomId.toString()).child(KEY_CONFIG).child(userRoot).setValue(userDbModel)
        }
    }

    fun writeGameStart(roomId: Int, isUserAStart: Boolean) {
        val gameStartRoot = if (isUserAStart) KEY_IS_USER_A_CONFIRM else KEY_IS_USER_B_CONFIRM
        d(TAG, "writeGameStart, roomId: $roomId, gameStartRoot: $gameStartRoot")
        rootRef.child(roomId.toString()).child(KEY_CONFIG).child(KEY_USER_CONFIRM_START).child(gameStartRoot).setValue(true)
    }

    fun readGameStart(roomId: Int, onReadGameStart: (isUserAReady: Boolean, isUserBReady: Boolean) -> Unit) {
        d(TAG, "readGameStart, roomId: $roomId")
        rootRef
                .child(roomId.toString())
                .child(KEY_CONFIG)
                .child(KEY_USER_CONFIRM_START)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        d(TAG, "readGameStart onCancelled")
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        d(TAG, "readGameStart onDataChange")
                        val userConfirmStartDbModel = dataSnapshot.getValue(UserConfirmStartDbModel::class.java)
                        if (userConfirmStartDbModel != null) {
                            with(userConfirmStartDbModel) {
                                onReadGameStart(userConfirmStartDbModel.isUserAConfirm, userConfirmStartDbModel.isUserBConfirm)
                            }

                        }
                    }

                })
    }

    fun readAnimalInfo(roomId: Int, onReadAnimalInfo: () -> Unit) {

        d(TAG, "readAnimalInfo, roomId: $roomId")

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_RAT)
                .addValueEventListener(mAnimalEventListenerA)

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_CAT)
                .addValueEventListener(mAnimalEventListenerA)

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_DOG)
                .addValueEventListener(mAnimalEventListenerA)

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_WOLF)
                .addValueEventListener(mAnimalEventListenerA)

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_LEOPARD)
                .addValueEventListener(mAnimalEventListenerA)

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_TIGER)
                .addValueEventListener(mAnimalEventListenerA)

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_LION)
                .addValueEventListener(mAnimalEventListenerA)

        rootRef
                .child(roomId.toString())
                .child(KEY_ANIMAL_INFO_A)
                .child(KEY_ELEPHANT)
                .addValueEventListener(mAnimalEventListenerA)
    }


    companion object {
        private val TAG = ChessStorageManager::class.java.simpleName
        private const val KEY_ROOT_DIR = "animal_chess_table_"
        private const val KEY_NEXT_ROOM_ID = "next_room_id"
        private const val INITIAL_ROOM_ID = 1
        private const val KEY_CLOUD_ANCHOR_CONFIG = "cloudAnchorConfig"
        private const val KEY_USER_A = "userA"
        private const val KEY_USER_B = "userB"
        private const val KEY_USER_CONFIRM_START = "userConfirmStart"
        private const val KEY_CONFIG = "config"
        private const val KEY_ROOM_ID = "roomId"
        private const val KEY_GAME_INFO = "gameInfo"
        private const val KEY_IS_USER_A_CONFIRM = "isUserAConfirm"
        private const val KEY_IS_USER_B_CONFIRM = "isUserBConfirm"
        private const val KEY_ANIMAL_INFO_A = "animalInfoA"
        private const val KEY_ANIMAL_INFO_B = "animalInfoB"
        private const val KEY_RAT = "rat"
        private const val KEY_CAT = "cat"
        private const val KEY_DOG = "dog"
        private const val KEY_WOLF = "wolf"
        private const val KEY_LEOPARD = "leopard"
        private const val KEY_TIGER = "tiger"
        private const val KEY_LION = "lion"
        private const val KEY_ELEPHANT = "elephant"

    }
}

class AnimalInfoUpdateListener : ValueEventListener {

    private val TAG = AnimalInfoUpdateListener::class.java.simpleName

    private lateinit var onAnimalUpdate: (isFromA: Boolean, animal: Animal) -> Unit

    fun setReadAnimalInfoListener(onReadAnimalInfo: (isFromA: Boolean, animal: Animal) -> Unit) {
        onAnimalUpdate = onReadAnimalInfo
    }

    override fun onCancelled(error: DatabaseError) {
        d(TAG, "AnimalInfoUpdateListener onCancelled")
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        d(TAG, "AnimalInfoUpdateListener onDataChange")
        val userConfirmStartDbModel = dataSnapshot.getValue(UserConfirmStartDbModel::class.java)
        if (userConfirmStartDbModel != null) {
            with(userConfirmStartDbModel) {
               // onAnimalUpdate(userConfirmStartDbModel.isUserAConfirm, userConfirmStartDbModel.isUserBConfirm)
            }

        }
    }

}