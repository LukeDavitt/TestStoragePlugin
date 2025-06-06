/*
 * Copyright 2024 Morpheus Data, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.datasets

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.data.DatasetInfo
import com.morpheusdata.core.data.DatasetQuery
import com.morpheusdata.core.providers.AbstractDatasetProvider
import io.reactivex.rxjava3.core.Observable
import groovy.util.logging.Slf4j

class TestStorageBucketDatasetProvider extends AbstractDatasetProvider<Map, Long> {
    public static final providerName = 'Test Storage Bucket Dataset Provider'
    public static final providerNamespace = 'example'
    public static final providerKey = 'testStorageBucketDatasetExample'
    public static final providerDescription = 'A collection of key/value pairs'

    static members = [
        [name:'Trinity', value:2],
        [name:'Cypher', value:3],
        [name:'Apoc', value:4],
        [name:'Switch', value:5],
        [name:'Dozer', value:6],
        [name:'Tank', value:7],
        [name:'Mouse', value:8],
        [name:'Link', value:9],
        [name:'Neo', value:1]
    ]

    TestStorageBucketDatasetProvider(Plugin plugin, MorpheusContext morpheus) {
        this.plugin = plugin
        this.morpheusContext = morpheus
    }

    @Override
    DatasetInfo getInfo() {
        new DatasetInfo(
            name: providerName,
            namespace: providerNamespace,
            key: providerKey,
            description: providerDescription
        )
    }

    Class<Map> getItemType() {
        return Map.class
    }

    Observable<Map> list(DatasetQuery query) {
        return Observable.fromIterable(members)
    }

    Observable<Map> listOptions(DatasetQuery query) {
        return Observable.fromIterable(members)
    }

    Map fetchItem(Object value) {
        def rtn = null
        if(value instanceof Long) {
            rtn = item((Long)value)
        } else if(value instanceof CharSequence) {
            def longValue = value.isNumber() ? value.toLong() : null
            if(longValue) {
                rtn = item(longValue)
            }
        }
        return rtn
    }

    Map item(Long value) {
        def rtn = members.find{ it.value == value }
        return rtn
    }

    String itemName(Map item) {
        return item.name
    }

    Long itemValue(Map item) {
        return (Long)item.value
    }

    @Override
    boolean isPlugin() {
        return true
    }
}
