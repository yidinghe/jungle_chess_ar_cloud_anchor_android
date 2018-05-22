package com.ar.animal.chess.util

import android.util.Log


/**
 * Created by yiding.he on 12/15/2016.
 * Only "e","d","i" is used in our project and only "e" and "d" are uploaded to TestFairy
 */
//fun Any.v(msg: () -> String) {
//    if (Log.isLoggable(tag, Log.VERBOSE)) v(msg())
//}

fun Any.d(msg: () -> String) {
    if (Log.isLoggable(tag, Log.DEBUG)) d(msg())
}

fun Any.i(msg: () -> String) {
    if (Log.isLoggable(tag, Log.INFO)) i(msg())
}

fun Any.e(msg: () -> String) {
    if (Log.isLoggable(tag, Log.ERROR)) e(msg())
}

//fun Any.wtf(msg: () -> String) {
//    w(msg())
//}

//fun Any.v(msg: String) {
//    v(tag, msg)
//}

fun Any.d(msg: String) {
    d(tag, msg)
}

fun Any.i(msg: String) {
    i(tag, msg)
}

//fun Any.w(msg: String) {
//    w(tag, msg)
//}

fun Any.e(msg: String) {
    e(tag, msg)
}

//fun Any.wtf(msg: String) {
//    wtf(tag, msg)
//}

//fun v(tag: String, msg: String) {
//    if(BuildConfig.DEBUG) {
//        Log.v(tag, msg)
//    }else{
//        TestFairy.log(tag, msg)
//    }
//}

fun d(tag: String, msg: String) {
    Log.d(tag, msg)
}

fun i(tag: String, msg: String) {
    Log.d(tag, msg)
}

//
fun e(tag: String, msg: String) {
    Log.e(tag, msg)
}


private val Any.tag: String
    get() = "ARCore"