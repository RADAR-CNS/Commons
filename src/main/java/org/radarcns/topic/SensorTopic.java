/*
 * Copyright 2017 The Hyve and King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.topic;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;

/**
 * AvroTopic used by sensors. This has additional verification on the schemas that are used compared
 * to AvroTopic.
 */
public class SensorTopic<K, V> extends AvroTopic<K, V> {
    public SensorTopic(String name, Schema keySchema, Schema valueSchema,
            Class<K> keyClass, Class<V> valueClass) {
        super(name, keySchema, valueSchema, keyClass, valueClass);

        if (keySchema.getType() != Type.RECORD) {
            throw new IllegalArgumentException("Sensors must send records as keys");
        }
        if (valueSchema.getType() != Type.RECORD) {
            throw new IllegalArgumentException("Sensors must send records as values");
        }

        if (keySchema.getField("userId") == null) {
            throw new IllegalArgumentException("Key schema must have a user ID");
        }
        if (keySchema.getField("sourceId") == null) {
            throw new IllegalArgumentException("Key schema must have a source ID");
        }
        if (valueSchema.getField("time") == null) {
            throw new IllegalArgumentException("Schema must have time as its first field");
        }
        if (valueSchema.getField("timeReceived") == null) {
            throw new IllegalArgumentException("Schema must have timeReceived as a field");
        }
    }
}