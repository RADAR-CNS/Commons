package org.radarcns.producer.rest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaValidationException;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.producer.rest.AvroDataMapper;
import org.radarcns.producer.rest.AvroDataMapperFactory;

public class AvroDataMapperFactoryTest {
    private static final Schema MEASUREMENT_KEY_SCHEMA = new Schema.Parser().parse("{"+
            "  \"namespace\": \"org.radarcns.key\","+
            "  \"type\": \"record\","+
            "  \"name\": \"MeasurementKey\","+
            "  \"doc\": \"Measurement key in the RADAR-CNS project\","+
            "  \"fields\": ["+
            "    {\"name\": \"userId\", \"type\": \"string\", \"doc\": \"user ID\"},"+
            "    {\"name\": \"sourceId\", \"type\": \"string\", \"doc\": \"device source ID\"}"+
            "  ]"+
            "}");

    private static final Schema INCOMPLETE_MEASUREMENT_KEY_SCHEMA = new Schema.Parser().parse("{"+
            "  \"namespace\": \"org.radarcns.key\","+
            "  \"type\": \"record\","+
            "  \"name\": \"MeasurementKey\","+
            "  \"doc\": \"Measurement key in the RADAR-CNS project\","+
            "  \"fields\": ["+
            "    {\"name\": \"sourceId\", \"type\": \"string\", \"doc\": \"device source ID\"}"+
            "  ]"+
            "}");

    private static final Schema SMALL_ENUM_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"E\",\"fields\":["
                    + "{\"name\": \"e\", \"type\": {\"type\": \"enum\", \"name\": \"Enum\", \"symbols\": [\"A\", \"B\"]}}"
                    + "]}");

    private static final Schema LARGE_ENUM_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"E\",\"fields\":["
                    +"{\"name\": \"e\", \"type\": {\"type\": \"enum\", \"name\": \"Enum\", \"symbols\": [\"A\", \"B\", \"C\"]}}"
                    + "]}");

    private static final Schema UNKNOWN_ENUM_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"E\",\"fields\":["
                    + "{\"name\": \"e\", \"type\": {\"type\": \"enum\", \"name\": \"Enum\", \"symbols\": [\"A\", \"B\", \"UNKNOWN\"]}}"
                    + "]}");

    private static final Schema DEFAULT_ENUM_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"E\",\"fields\":["
                    + "{\"name\": \"e\", \"type\": {\"type\": \"enum\", \"name\": \"Enum\", \"symbols\": [\"A\"]}, \"default\": \"A\"}"
                    + "]}");

    private static final Schema ALL_TYPES_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"R\",\"fields\":["
                    + "{\"name\": \"e\", \"type\": {\"type\": \"enum\", \"name\": \"Enum\", \"symbols\": [\"A\"]}, \"default\": \"A\"},"
                    + "{\"name\": \"i\", \"type\": \"int\"},"
                    + "{\"name\": \"l\", \"type\": \"long\"},"
                    + "{\"name\": \"d\", \"type\": \"double\"},"
                    + "{\"name\": \"f\", \"type\": \"float\"},"
                    + "{\"name\": \"sI\", \"type\": \"string\"},"
                    + "{\"name\": \"sD\", \"type\": \"string\"},"
                    + "{\"name\": \"sU\", \"type\": [\"null\", \"string\"]},"
                    + "{\"name\": \"sUi\", \"type\": [\"null\", \"string\"]},"
                    + "{\"name\": \"sUe\", \"type\": [\"null\", {\"name\": \"SE\", \"type\": \"enum\", \"symbols\": [\"A\"]}]},"
                    + "{\"name\": \"uS\", \"type\": \"string\"},"
                    + "{\"name\": \"se2\", \"type\": \"string\"},"
                    + "{\"name\": \"se3\", \"type\": \"string\"},"
                    + "{\"name\": \"a\", \"type\": {\"type\":\"array\", \"items\": {\"type\": \"int\"}}},"
                    + "{\"name\": \"m\", \"type\": {\"type\":\"map\", \"values\": {\"type\": \"int\"}}},"
                    + "{\"name\": \"unmapped\", \"type\": \"int\"}"
                    + "]}");
    private static final Schema ALL_TYPES_ALT_SCHEMA = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"R\",\"fields\":["
                    + "{\"name\": \"e\", \"type\": {\"type\": \"enum\", \"name\": \"Enum\", \"symbols\": [\"A\", \"B\"]}, \"default\": \"A\"},"
                    + "{\"name\": \"i\", \"type\": \"long\"},"
                    + "{\"name\": \"l\", \"type\": \"double\"},"
                    + "{\"name\": \"d\", \"type\": \"float\"},"
                    + "{\"name\": \"f\", \"type\": \"double\"},"
                    + "{\"name\": \"sI\", \"type\": \"int\", \"default\": 0},"
                    + "{\"name\": \"sD\", \"type\": \"double\", \"default\": 0.0},"
                    + "{\"name\": \"sU\", \"type\": \"string\", \"default\": \"\"},"
                    + "{\"name\": \"sUi\", \"type\": [\"null\", \"int\"], \"default\":null},"
                    + "{\"name\": \"sUe\", \"type\": {\"name\": \"SE\", \"type\": \"enum\", \"symbols\": [\"A\"]}, \"default\": \"A\"},"
                    + "{\"name\": \"uS\", \"type\": [\"null\", \"string\"]},"
                    + "{\"name\": \"se2\", \"type\": {\"name\": \"SE2\", \"type\": \"enum\", \"symbols\": [\"A\", \"B\"]}, \"default\": \"A\"},"
                    + "{\"name\": \"se3\", \"type\": {\"name\": \"SE3\", \"type\": \"enum\", \"symbols\": [\"A\", \"B\"]}, \"default\": \"A\"},"
                    + "{\"name\": \"a\", \"type\": {\"type\":\"array\", \"items\": {\"type\": \"float\"}}},"
                    + "{\"name\": \"m\", \"type\": {\"type\":\"map\", \"values\": {\"type\": \"float\"}}}"
                    + "]}");
    private AvroDataMapperFactory factory;

    @Before
    public void setUp() {
        this.factory = new AvroDataMapperFactory();
    }

    @Test
    public void mapRecord() throws SchemaValidationException, IOException {
        String actual = doMap(MEASUREMENT_KEY_SCHEMA, ObservationKey.getClassSchema(),
                "{\"userId\":\"u\", \"sourceId\": \"s\"}");
        assertEquals("{\"projectId\":null,\"userId\":\"u\",\"sourceId\":\"s\"}", actual);
    }

    @Test(expected = SchemaValidationException.class)
    public void mapRecordIncomplete() throws SchemaValidationException {
        factory.createMapper(INCOMPLETE_MEASUREMENT_KEY_SCHEMA, ObservationKey.getClassSchema(), null);
    }

    @Test
    public void mapEnumLarger() throws SchemaValidationException, IOException {
        String actual = doMap(SMALL_ENUM_SCHEMA, LARGE_ENUM_SCHEMA, "{\"e\":\"A\"}");
        assertEquals("{\"e\":\"A\"}", actual);
    }

    @Test(expected = SchemaValidationException.class)
    public void mapEnumSmaller() throws SchemaValidationException {
        factory.createMapper(LARGE_ENUM_SCHEMA, SMALL_ENUM_SCHEMA, null);
    }

    @Test
    public void mapEnumSmallerUnknown() throws SchemaValidationException, IOException {
        String actual = doMap(LARGE_ENUM_SCHEMA, UNKNOWN_ENUM_SCHEMA, "{\"e\":\"C\"}");
        assertEquals("{\"e\":\"UNKNOWN\"}", actual);
    }


    @Test
    public void mapEnumSmallerDefault() throws SchemaValidationException, IOException {
        String actual = doMap(LARGE_ENUM_SCHEMA, DEFAULT_ENUM_SCHEMA, "{\"e\":\"C\"}");
        assertEquals("{\"e\":\"A\"}", actual);
    }

    @Test
    public void mapAll() throws SchemaValidationException, IOException {
        String actual = doMap(ALL_TYPES_SCHEMA, ALL_TYPES_ALT_SCHEMA, "{" +
                "\"e\":\"A\"," +
                "\"i\":1," +
                "\"l\":2," +
                "\"d\":3.0," +
                "\"f\":4.0," +
                "\"sI\":\"5\"," +
                "\"sD\":\"6.5\"," +
                "\"sU\":null," +
                "\"sUi\":{\"string\":\"7\"}," +
                "\"sUe\":null," +
                "\"uS\":\"s\"," +
                "\"se2\":\"B\"," +
                "\"se3\":\"g\"," +
                "\"a\":[1,2]," +
                "\"m\":{\"a\":9}," +
                "\"unmapped\":10}");

        assertEquals("{" +
                "\"e\":\"A\"," +
                "\"i\":1," +
                "\"l\":2.0," +
                "\"d\":3.0," +
                "\"f\":4.0," +
                "\"sI\":5," +
                "\"sD\":6.5," +
                "\"sU\":\"\"," +
                "\"sUi\":{\"int\":7}," +
                "\"sUe\":\"A\"," +
                "\"uS\":{\"string\":\"s\"}," +
                "\"se2\":\"B\"," +
                "\"se3\":\"A\"," +
                "\"a\":[1.0,2.0]," +
                "\"m\":{\"a\":9.0}}", actual);

    }

    private String doMap(Schema from, Schema to, String value)
            throws IOException, SchemaValidationException {
        AvroDataMapper mapper = factory.createMapper(from, to, null);

        GenericDatumReader<Object> reader = new GenericDatumReader<>(from);
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(from, value);
        Object readValue = reader.read(null, decoder);

        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(to);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(to, out);
        writer.write(mapper.convertAvro(readValue), encoder);
        encoder.flush();
        return out.toString("utf-8");
    }
}
