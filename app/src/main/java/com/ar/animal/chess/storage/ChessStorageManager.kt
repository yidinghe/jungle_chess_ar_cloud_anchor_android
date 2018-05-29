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

    companion object {
        private val TAG = ChessStorageManager::class.java.simpleName
        private val KEY_ROOT_DIR = "animal_chess_table_"
        private val KEY_NEXT_ROOM_ID = "next_room_id"
        private val INITIAL_ROOM_ID = 1
        private val KEY_CLOUD_ANCHOR_CONFIG = "cloudAnchorConfig"
        private val KEY_USER_A = "userA"
        private val KEY_USER_B = "userB"
        private val KEY_USER_CONFIRM_START = "userConfirmStart"
        private val KEY_CONFIG = "config"
        private val KEY_ROOM_ID = "roomId"
        private val KEY_GAME_INFO = "gameInfo"
    }
}