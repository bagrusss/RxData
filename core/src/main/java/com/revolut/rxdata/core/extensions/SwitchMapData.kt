package com.revolut.rxdata.core.extensions

import com.revolut.rxdata.core.CompositeException
import com.revolut.rxdata.core.Data
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/*
 * Copyright (C) 2020 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * Switch maps Observable<Data<T>> stream into Observable<Data<R>> invoking given block only if data is not null
 * in emitted item, otherwise Data<R> will be emitted with null data keeping errors/loading fields.
 */
fun <T, R> Observable<Data<T>>.switchMapDataContent(block: (T) -> Observable<R>): Observable<Data<R>> =
    switchMap { original ->
        if (original.content != null) {
            try {
                block(original.content).map { transformed ->
                    transformed.toData(
                        loading = original.loading,
                        error = original.error
                    )
                }
            } catch (t: Throwable) {
                Observable.just(
                    Data<R>(
                        error = combineErrors(original.error, t),
                        loading = original.loading
                    )
                )
            }
        } else {
            Observable.just(
                Data(
                    error = original.error,
                    loading = original.loading
                )
            )
        }
    }

fun <T, R> Observable<Data<T>>.switchMapDataContentSingle(block: (T) -> Single<R>): Observable<Data<R>> =
    switchMapSingle { original ->
        if (original.content != null) {
            try {
                block(original.content).map { transformed ->
                    transformed.toData(
                        loading = original.loading,
                        error = original.error
                    )
                }
            } catch (t: Throwable) {
                Single.just(
                    Data<R>(
                        error = combineErrors(original.error, t),
                        loading = original.loading
                    )
                )
            }
        } else {
            Single.just(
                Data(
                    error = original.error,
                    loading = original.loading
                )
            )
        }
    }

fun <T, R> Observable<Data<T>>.switchMapData(block: (T) -> Observable<Data<R>>): Observable<Data<R>> =
    switchMap { original ->
        if (original.content != null) {
            try {
                block(original.content)
            } catch (t: Throwable) {
                Observable.just(
                    Data<R>(
                        error = combineErrors(original.error, t),
                        loading = original.loading
                    )
                )
            }
        } else {
            Observable.just(
                Data(
                    error = original.error,
                    loading = original.loading
                )
            )
        }
    }

private fun combineErrors(
    firstError: Throwable?,
    secondError: Throwable?
): Throwable? {
    return when {
        firstError != null && secondError != null -> CompositeException(
            listOf(
                firstError,
                secondError
            )
        )
        firstError != null -> firstError
        secondError != null -> secondError
        else -> null
    }
}

private fun <T> T?.toData(loading: Boolean = false, error: Throwable? = null): Data<T> =
    Data(
        content = this,
        error = error,
        loading = loading
    )