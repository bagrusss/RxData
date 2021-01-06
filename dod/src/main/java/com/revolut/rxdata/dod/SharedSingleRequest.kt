package com.revolut.rxdata.dod

import io.reactivex.rxjava3.core.Single

/*
 * Copyright (C) 2019 Revolut
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

internal class SharedSingleRequest<Params, Result : Any>(
    private val load: (params: Params) -> Single<Result>
) {

    private val sharedObservableRequest: SharedObservableRequest<Params, Result> =
        SharedObservableRequest { params ->
            load(params).toObservable()
        }

    fun getOrLoad(params: Params): Single<Result> =
        sharedObservableRequest.getOrLoad(params).firstOrError()

}