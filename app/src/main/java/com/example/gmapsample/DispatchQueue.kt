package com.example.gmapsample

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import java.util.concurrent.CountDownLatch


class DispatchQueue: Thread {

    @Volatile
    private var handler: Handler? = null
    private val syncLatch: CountDownLatch = CountDownLatch(1)
    private var lastTaskTime: Long = 0

    constructor(threadName: String?) : this(threadName, true){}

    constructor(threadName: String?, start: Boolean) {
        name = threadName
        if (start) {
            start()
        }
    }

    fun sendMessage(msg: Message?, delay: Int) {
        try {
            syncLatch.await()
            if (delay <= 0) {
                handler!!.sendMessage(msg!!)
            } else {
                handler!!.sendMessageDelayed(msg!!, delay.toLong())
            }
        } catch (ignore: Exception) {
        }
    }

    fun cancelRunnable(runnable: Runnable?) {
        try {
            syncLatch.await()
            handler!!.removeCallbacks(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelRunnables(runnables: Array<Runnable?>) {
        try {
            syncLatch.await()
            for (i in runnables.indices) {
                handler!!.removeCallbacks(runnables[i])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun postRunnable(runnable: Runnable?) {
        postRunnable(runnable, 0)
        lastTaskTime = SystemClock.elapsedRealtime()
    }

    fun postRunnable(runnable: Runnable?, delay: Long) {
        try {
            syncLatch.await()
        } catch (e: Exception) {
           e.printStackTrace()
        }
        if (delay <= 0) {
            handler!!.post(runnable)
        } else {
            handler!!.postDelayed(runnable, delay)
        }
    }

    fun cleanupQueue() {
        try {
            syncLatch.await()
            handler!!.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleMessage(inputMessage: Message?) {}

    fun getLastTaskTime(): Long {
        return lastTaskTime
    }

    fun recycle() {
        handler!!.looper.quit()
    }

    override fun run() {
        Looper.prepare()
        handler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                this@DispatchQueue.handleMessage(msg)
            }
        }
        syncLatch.countDown()
        Looper.loop()
    }
}