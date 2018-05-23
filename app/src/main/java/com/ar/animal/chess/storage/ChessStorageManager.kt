package com.ar.animal.chess.storage

import android.content.Context
import android.util.Log
import com.ar.animal.chess.model.ChessDbModel
import com.ar.animal.chess.model.ConfigDbModel
import com.ar.animal.chess.util.ChessConstants

import com.google.firebase.FirebaseApp
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

    /**
     * Listener for a new Cloud Anchor ID from the Firebase Database.
     */
    internal interface CloudAnchorIdListener {
        fun onCloudAnchorIdAvailable(cloudAnchorId: String?)
    }

    /**
     * Listener for a new short code from the Firebase Database.
     */
    internal interface ShortCodeListener {
        fun onShortCodeAvailable(shortCode: Int?)
    }

    init {
        val rootDir = KEY_ROOT_DIR + ChessConstants.END_POINT.name
        rootRef = FirebaseDatabase.getInstance().reference.child(rootDir)
        DatabaseReference.goOnline()
    }

    /**
     * Gets a new short code that can be used to store the anchor ID.
     */
    fun nextRoomId(listener: ShortCodeListener) {
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
                                    listener.onShortCodeAvailable(null)
                                } else {
                                    if (currentData?.value == null) {
                                        listener.onShortCodeAvailable(null)
                                    } else {
                                        val roomId = currentData.value as Long
                                        listener.onShortCodeAvailable(roomId.toInt())
                                    }

                                }
                            }
                        })
    }

    /**
     * Stores the cloud anchor ID in the configured Firebase Database.
     */
    fun storeCloudAnchorIdUsingRoomId(shortCode: Int, cloudAnchorId: String) {
        val configDbModel = ConfigDbModel(cloudAnchorId, timestamp = System.currentTimeMillis().toString())
        val chessDbModel = ChessDbModel(shortCode, configDbModel)
        rootRef.child(shortCode.toString()).setValue(chessDbModel)
    }

    /**
     * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
     * was not stored for this short code.
     */
    fun getCloudAnchorId(shortCode: Int, listener: CloudAnchorIdListener) {
        rootRef
                .child(shortCode.toString())
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val chessDbModel = dataSnapshot.getValue(ChessDbModel::class.java)
                                if (chessDbModel != null)
                                    listener.onCloudAnchorIdAvailable(chessDbModel.config.cloudAnchorId)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(TAG, "The database operation for getCloudAnchorId was cancelled.",
                                        error.toException())
                                listener.onCloudAnchorIdAvailable(null)
                            }
                        })
    }

    companion object {
        private val TAG = ChessStorageManager::class.java.simpleName
        private val KEY_ROOT_DIR = "animal_chess_table_"
        private val KEY_NEXT_ROOM_ID = "next_room_id"
        private val INITIAL_SHORT_CODE = 1
    }
}