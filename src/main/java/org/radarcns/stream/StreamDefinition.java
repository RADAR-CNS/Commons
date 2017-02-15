/*
 * Copyright 2017 Kings College London and The Hyve
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

package org.radarcns.stream;

import javax.annotation.Nonnull;
import org.radarcns.topic.KafkaTopic;

public class StreamDefinition {
    private final KafkaTopic inputTopic;
    private final KafkaTopic outputTopic;

    public StreamDefinition(@Nonnull KafkaTopic input, @Nonnull KafkaTopic output) {
        this.inputTopic = input;
        this.outputTopic = output;
    }

    public KafkaTopic getInputTopic() {
        return inputTopic;
    }

    public KafkaTopic getOutputTopic() {
        return outputTopic;
    }

    public String getStateStoreName() {
        return getInputTopic().getName() + "->" + getOutputTopic().getName();
    }
}
