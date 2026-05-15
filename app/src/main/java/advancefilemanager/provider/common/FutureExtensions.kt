/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T, R> Future<T>.map(
    transform: (T) -> R,
    transformException: (Exception) -> Exception = { it }
): Future<R> =
    object : Future<R> {
        override fun cancel(mayInterruptIfRunning: Boolean): Boolean =
            this@map.cancel(mayInterruptIfRunning)

        override fun isCancelled(): Boolean = this@map.isCancelled

        override fun isDone(): Boolean = this@map.isDone

        @Throws(ExecutionException::class, InterruptedException::class)
        override fun get(): R = transformGet { this@map.get() }

        @Throws(ExecutionException::class, InterruptedException::class, TimeoutException::class)
        override fun get(timeout: Long, unit: TimeUnit): R =
            transformGet { this@map.get(timeout, unit) }

        @Throws(ExecutionException::class, InterruptedException::class, TimeoutException::class)
        private inline fun transformGet(get: () -> T): R {
            val result = try {
                get()
            } catch (e: Exception) {
                val exception = try {
                    transformException(e)
                } catch (e2: Exception) {
                    e2.addSuppressed(e)
                    throw ExecutionException(e2)
                }
                check(
                    exception is ExecutionException || exception is InterruptedException ||
                        exception is TimeoutException
                )
                throw exception
            }
            try {
                return transform(result)
            } catch (e: Exception) {
                throw ExecutionException(e)
            }
        }
    }

fun <T> Deferred<T>.asFuture(): Future<T> =
    object : Future<T> {
        private val latch = CountDownLatch(1)

        init {
            invokeOnCompletion { latch.countDown() }
        }

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            cancel()
            return this@asFuture.isCancelled
        }

        override fun isCancelled(): Boolean = this@asFuture.isCancelled

        override fun isDone(): Boolean = isCompleted

        @Throws(ExecutionException::class, InterruptedException::class)
        override fun get(): T {
            latch.await()
            return getCompleted()
        }

        @Throws(ExecutionException::class, InterruptedException::class, TimeoutException::class)
        override fun get(timeout: Long, unit: TimeUnit): T {
            latch.await(timeout, unit)
            return getCompleted()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Throws(ExecutionException::class)
        private fun getCompleted(): T =
            try {
                this@asFuture.getCompleted()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw ExecutionException(e)
            }
    }
