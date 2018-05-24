package com.ar.animal.chess.storage

import android.util.Log
import com.ar.animal.chess.model.ChessDbModel
import com.ar.animal.chess.model.ChessUserInfo
import com.ar.animal.chess.model.ConfigDbModel
import com.ar.animal.chess.model.UserType
import com.ar.animal.chess.util.ChessConstants

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
internal class ChessStorageManager() {
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
        // Run a transaction on the node containing the next short code available. This increments the
        // value in the database and retrieves it in one atomic all-or-nothing operation.
        rootRef
                .child(KEY_NEXT_ROOM_ID)
                .runTransaction(
                        object : Transaction.Handler {

                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                var shortCode = currentData.getValue(Int::class.java)
                                if (shortCode == null) {
                                    shortCode = INITIAL_SHORT_CODE - 1
                                }
                                currentData.value = shortCode + 1
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(
                                    error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                if (!committed) {
                                    Log.e(TAG, "Firebase Error", error?.toException())
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
        val configDbModel = ConfigDbModel(cloudAnchorId, timestamp = System.currentTimeMillis().toString())
        rootRef.child(shortCode.toString()).child("config").setValue(configDbModel)
    }

    /**
     * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
     * was not stored for this short code.
     */
    fun readCloudAnchorId(shortCode: Int, onReadCloudAnchorId: (cloudAnchorId: String?) -> Unit) {
        rootRef
                .child(shortCode.toString())
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val chessDbModel = dataSnapshot.getValue(ChessDbModel::class.java)
                                if (chessDbModel != null)
                                    onReadCloudAnchorId(chessDbModel.config.cloudAnchorId)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(TAG, "The database operation for readCloudAnchorId was cancelled.",
                                        error.toException())
                                onReadCloudAnchorId(null)
                            }
                        })
    }

    // this function for UserA listen UserB online event to start Game, and UserB grab UserA info to show the board

    fun readUserInfo(roomId: Int, isUserA: Boolean, onReadUserInfo: (userInfo: ChessUserInfo) -> Unit) {
        val userRoot = if (isUserA) "userA" else "userB"
        rootRef
                .child(roomId.toString())
                .child("config")
                .child(userRoot)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                    }

                })
    }

    fun writeUserInfo(roomId: Int, userInfo: ChessUserInfo) {
        val userRoot = if (userInfo.userType == UserType.USER_A) "userA" else "userB"
        rootRef.child(roomId.toString()).child("config").child(userRoot).setValue(userInfo)
    }

    companion object {
        private val TAG = ChessStorageManager::class.java.simpleName
        private val KEY_ROOT_DIR = "animal_chess_table_"
        private val KEY_NEXT_ROOM_ID = "next_room_id"
        private val INITIAL_SHORT_CODE = 1
    }
}