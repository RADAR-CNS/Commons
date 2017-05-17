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

package org.radarcns.stream.collector;

import static org.radarcns.util.Serialization.floatToDouble;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Java class to aggregate data using Kafka Streams. Double is the base unit */
public class DoubleValueCollector {
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private BigDecimal sum = BigDecimal.ZERO;
    private int count = 0;
    private double avg = 0;
    private final Double[] quartile = new Double[3];
    private double iqr = 0;

    private final List<Double> history = new ArrayList<>();

    public DoubleValueCollector add(float value) {
        return this.add(floatToDouble(value));
    }

    /**
     * @param value new sample that has to be analysed
     */
    public DoubleValueCollector add(double value) {
        updateMin(value);
        updateMax(value);
        updateAvg(value);
        updateQuartile(value);

        return this;
    }

    /**
     * @param value new sample that update min value
     */
    private void updateMin(double value) {
        if (min > value) {
            min = value;
        }
    }

    /**
     * @param value new sample that update max value
     */
    private void updateMax(double value) {
        if (max < value) {
            max = value;
        }
    }

    /**
     * @param value new sample that update average value
     */
    private void updateAvg(double value) {
        count++;
        sum = sum.add(BigDecimal.valueOf(value));

        avg = sum.doubleValue() / count;
    }

    /**
     * @param value new sample that update quartiles value
     */
    private void updateQuartile(double value) {
        history.add(value);
        Collections.sort(history);

        int length = history.size();

        if (length == 1) {
            quartile[0] = quartile[1] = quartile[2] = history.get(0);
        } else {
            for (int i = 0; i < 3; i++) {
                double pos = (i + 1) * (length + 1) / 4.0d;  // == (i + 1) * 25 * (length + 1) / 100
                int intPos = (int) pos;
                if (intPos == 0) {
                    quartile[i] = history.get(0);
                } else if (intPos == length) {
                    quartile[i] = history.get(length - 1);
                } else {
                    double diff = pos - intPos;
                    double base = history.get(intPos - 1);
                    quartile[i] = base + diff * (history.get(intPos) - base);
                }
            }
        }

        iqr = BigDecimal.valueOf(quartile[2]).subtract(
                BigDecimal.valueOf(quartile[0])).doubleValue();
    }

    @Override
    public String toString() {
        return "DoubleValueCollector{"
                + "min=" + getMin()
                + ", max=" + getMax()
                + ", sum=" + getSum()
                + ", count=" + getCount()
                + ", avg=" + getAvg()
                + ", quartile=" + getQuartile()
                + ", iqr=" + getIqr()
                + ", history=" + history + '}';
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getSum() {
        return sum.doubleValue();
    }

    public double getCount() {
        return count;
    }

    public double getAvg() {
        return avg;
    }

    public List<Double> getQuartile() {
        return  Arrays.asList(quartile);
    }

    public double getIqr() {
        return iqr;
    }
}
