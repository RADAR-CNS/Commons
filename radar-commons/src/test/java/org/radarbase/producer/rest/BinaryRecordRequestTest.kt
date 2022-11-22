package org.radarbase.producer.rest

import okio.Buffer
import org.apache.avro.SchemaValidationException
import org.apache.avro.io.BinaryEncoder
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.junit.Assert
import org.junit.Test
import org.radarbase.data.AvroRecordData
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.radarcns.kafka.RecordSet
import org.radarcns.passive.empatica.EmpaticaE4BloodVolumePulse
import org.radarcns.passive.phone.PhoneAcceleration
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.util.zip.GZIPOutputStream

class BinaryRecordRequestTest {
    @Test
    @Throws(SchemaValidationException::class, IOException::class)
    fun writeToStream() {
        val k = ObservationKey("test", "a", "b")
        val v = EmpaticaE4BloodVolumePulse(
            0.0, 0.0,
            0.0f
        )
        val t = AvroTopic(
            "t", k.schema, v.schema, k.javaClass, v.javaClass
        )
        val request = BinaryRecordRequest(t)
        request.prepare(
            ParsedSchemaMetadata(2, 1, k.schema),
            ParsedSchemaMetadata(4, 2, v.schema),
            AvroRecordData(t, k, listOf(v))
        )
        val buffer = Buffer()
        request.writeToSink(buffer)
        Assert.assertArrayEquals(EXPECTED, buffer.readByteArray())
    }

    @Test
    @Throws(IOException::class)
    fun expectedMatchesRecordSet() {
        val recordSet = RecordSet.newBuilder()
            .setKeySchemaVersion(1)
            .setValueSchemaVersion(2)
            .setData(
                listOf(
                    ByteBuffer.wrap(
                        byteArrayOf(
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0
                        )
                    )
                )
            )
            .setProjectId(null)
            .setUserId(null)
            .setSourceId("b")
            .build()
        val writer = SpecificDatumWriter<RecordSet>(RecordSet.`SCHEMA$`)
        val out = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(out, null)
        writer.write(recordSet, encoder)
        encoder.flush()
        Assert.assertArrayEquals(EXPECTED, out.toByteArray())
    }

    @Test
    @Throws(IOException::class)
    fun testSize() {
        val writer = SpecificDatumWriter<PhoneAcceleration>(PhoneAcceleration.`SCHEMA$`)
        val records: MutableList<ByteBuffer> = ArrayList(540)
        requireNotNull(BinaryRecordRequestTest::class.java.getResourceAsStream("android_phone_acceleration.csv"))
            .use { stream ->
                InputStreamReader(stream).use { reader ->
                    BufferedReader(reader).use { br ->
                        var line = br.readLine()
                        var encoder: BinaryEncoder? = null
                        while (line != null) {
                            val values = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            val acc = PhoneAcceleration(
                                values[0].toDouble(),
                                values[1].toDouble(),
                                values[2].toFloat(),
                                values[3].toFloat(),
                                values[4].toFloat()
                            )
                            val out = ByteArrayOutputStream()
                            encoder = EncoderFactory.get().binaryEncoder(out, encoder)
                            writer.write(acc, encoder)
                            encoder.flush()
                            records.add(ByteBuffer.wrap(out.toByteArray()))
                            line = br.readLine()
                        }
                    }
                }
            }
        val recordSet = RecordSet.newBuilder()
            .setKeySchemaVersion(1)
            .setValueSchemaVersion(2)
            .setData(records)
            .setProjectId(null)
            .setUserId(null)
            .setSourceId("596740ca-5875-4c97-87ab-a08405f36aff")
            .build()
        val recordWriter = SpecificDatumWriter<RecordSet>(RecordSet.`SCHEMA$`)
        val out = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(out, null)
        recordWriter.write(recordSet, encoder)
        encoder.flush()
        println("Size of record set with " + records.size + " entries: " + out.size())
        val gzippedOut = ByteArrayOutputStream()
        val gzipOut = GZIPOutputStream(gzippedOut)
        gzipOut.write(out.size())
        gzipOut.close()
        println("Gzipped size of record set with " + records.size + " entries: " + gzippedOut.size())
    }

    companion object {
        // note that positive numbers are multiplied by two in avro binary encoding, due to the
        // zig-zag encoding schema used.
        // See http://avro.apache.org/docs/1.8.1/spec.html#binary_encoding
        private val EXPECTED = byteArrayOf(
            2,  // key version x2
            4,  // value version x2
            0,  // null project ID
            0,  // null user ID
            2, 'b'.code.toByte(),  // string length x2, sourceId
            2,  // number of records x2
            40,  // number of bytes in the first value x2
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  // value
            0 // end of array
        )
    }
}
