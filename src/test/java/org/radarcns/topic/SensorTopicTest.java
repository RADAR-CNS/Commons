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

import static org.junit.Assert.*;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;

/**
 * Created by joris on 05/07/2017.
 */
public class SensorTopicTest {

    @Test
    public void workingConstructor() {
        Schema keySchema = SchemaBuilder.record("key").fields()
                .name("userId").type(Schema.create(Type.STRING)).noDefault()
                .name("sourceId").type(Schema.create(Type.STRING)).noDefault()
                .endRecord();
        Schema valueSchema = SchemaBuilder.record("value").fields()
                .name("time").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("timeReceived").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("value").type(Schema.create(Type.DOUBLE)).noDefault()
                .endRecord();

        new SensorTopic<>("test",
                keySchema, valueSchema,
                GenericRecord.class, GenericRecord.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullArguments() {
        new SensorTopic<>(null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullArgumentsExceptName() {
        new SensorTopic<>("test", null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullName() {
        Schema keySchema = SchemaBuilder.record("key").fields()
                .name("userId").type(Schema.create(Type.STRING)).noDefault()
                .name("sourceId").type(Schema.create(Type.STRING)).noDefault()
                .endRecord();
        Schema valueSchema = SchemaBuilder.record("value").fields()
                .name("time").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("timeReceived").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("value").type(Schema.create(Type.DOUBLE)).noDefault()
                .endRecord();

        new SensorTopic<>(null,
                keySchema, valueSchema,
                GenericRecord.class, GenericRecord.class);
    }


    @Test(expected = IllegalArgumentException.class)
    public void missingUserId() {
        Schema keySchema = SchemaBuilder.record("key").fields()
                .name("sourceId").type(Schema.create(Type.STRING)).noDefault()
                .endRecord();
        Schema valueSchema = SchemaBuilder.record("value").fields()
                .name("time").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("timeReceived").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("value").type(Schema.create(Type.DOUBLE)).noDefault()
                .endRecord();

        new SensorTopic<>("test",
                keySchema, valueSchema,
                GenericRecord.class, GenericRecord.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingTime() {
        Schema keySchema = SchemaBuilder.record("key").fields()
                .name("userId").type(Schema.create(Type.STRING)).noDefault()
                .name("sourceId").type(Schema.create(Type.STRING)).noDefault()
                .endRecord();
        Schema valueSchema = SchemaBuilder.record("value").fields()
                .name("timeReceived").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("value").type(Schema.create(Type.DOUBLE)).noDefault()
                .endRecord();

        new SensorTopic<>("test",
                keySchema, valueSchema,
                GenericRecord.class, GenericRecord.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notARecord() {
        Schema keySchema = Schema.create(Type.STRING);
        Schema valueSchema = SchemaBuilder.record("value").fields()
                .name("timeReceived").type(Schema.create(Type.DOUBLE)).noDefault()
                .name("value").type(Schema.create(Type.DOUBLE)).noDefault()
                .endRecord();

        new SensorTopic<>("test",
                keySchema, valueSchema,
                GenericRecord.class, GenericRecord.class);
    }
}