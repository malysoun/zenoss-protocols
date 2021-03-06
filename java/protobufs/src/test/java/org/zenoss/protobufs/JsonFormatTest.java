/*****************************************************************************
 * 
 * Copyright (C) Zenoss, Inc. 2010-2011, all rights reserved.
 * 
 * This content is made available according to terms specified in
 * License.zenoss under the directory where your Zenoss product is installed.
 * 
 ****************************************************************************/


package org.zenoss.protobufs;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.UnknownFieldSet;
import org.junit.Test;
import org.zenoss.protobufs.test.JsonFormatProtos;
import org.zenoss.protobufs.test.JsonFormatProtos.JsonFormatEnum;
import org.zenoss.protobufs.test.JsonFormatProtos.JsonFormatMessage1;

import com.google.protobuf.ByteString;

public class JsonFormatTest {

    private JsonFormatMessage1.Builder createMessageBuilder() {
        Random r = new Random();
        JsonFormatMessage1.Builder builder = JsonFormatMessage1.newBuilder();
        builder.setBoolField(true);
        byte[] bytes = new byte[128];
        r.nextBytes(bytes);
        builder.setBytesField(ByteString.copyFrom(bytes));
        builder.setDoubleField(r.nextDouble());
        builder.setEnumField(JsonFormatEnum.JSON_FORMAT_ENUM_VAL2);
        builder.setFixed32Field(r.nextInt());
        builder.setFixed64Field(r.nextLong());
        builder.setFloatField(r.nextFloat());
        builder.setInt32Field(r.nextInt());
        builder.setInt64Field(r.nextLong());
        builder.setSfixed32Field(r.nextInt());
        builder.setSfixed64Field(r.nextLong());
        builder.setSint32Field(r.nextInt());
        builder.setSint64Field(r.nextLong());
        builder.setStrField("String" + r.nextInt());
        builder.setUint32Field(r.nextInt());
        builder.setUint64Field(r.nextLong());
        return builder;
    }

    private JsonFormatMessage1 createMessage() {
        return createMessageBuilder().build();
    }

    @Test
    public void testJsonFormat() throws IOException {
        JsonFormatMessage1 msg = createMessage();

        // Test input stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonFormat.writeTo(msg, baos);
        assertEquals(msg, JsonFormat.merge(
                new ByteArrayInputStream(baos.toByteArray()),
                JsonFormatMessage1.newBuilder()));

        // Test reader
        StringWriter sw = new StringWriter();
        JsonFormat.writeTo(msg, sw);
        assertEquals(msg, JsonFormat.merge(new StringReader(sw.toString()),
                JsonFormatMessage1.newBuilder()));

        // Test string
        String json = JsonFormat.writeAsString(msg);
        assertEquals(msg,
                JsonFormat.merge(json, JsonFormatMessage1.newBuilder()));
    }

    @Test
    public void testJsonFormatDelimited() throws IOException {
        List<JsonFormatMessage1> messages = new ArrayList<JsonFormatMessage1>();
        for (int i = 0; i < 10; i++) {
            messages.add(createMessage());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonFormat.writeAllDelimitedTo(messages, baos);

        List<JsonFormatMessage1> decoded = JsonFormat.mergeAllDelimitedFrom(
                new ByteArrayInputStream(baos.toByteArray()),
                JsonFormatMessage1.getDefaultInstance());
        assertEquals(messages, decoded);

        // Test reader
        StringWriter sw = new StringWriter();
        JsonFormat.writeAllDelimitedTo(messages, sw);
        assertEquals(messages, JsonFormat.mergeAllDelimitedFrom(
                new StringReader(sw.toString()),
                JsonFormatMessage1.getDefaultInstance()));

        // Test string
        String json = JsonFormat.writeAllDelimitedAsString(messages);
        assertEquals(
                messages,
                JsonFormat.mergeAllDelimitedFrom(json,
                        JsonFormatMessage1.getDefaultInstance()));
    }

    @Test
    public void testExtensions() throws IOException {
        JsonFormatMessage1.Builder message1Builder = createMessageBuilder();
        message1Builder.setExtension(JsonFormatProtos.extField, "myvalue");
        JsonFormatMessage1 message = message1Builder.build();

        // Verify that encoding/decoding without an extension registry works
        StringWriter sw = new StringWriter();
        JsonFormat.writeTo(message, sw);
        assertEquals(JsonFormatMessage1.newBuilder(message).clearExtension(JsonFormatProtos.extField).build(),
                JsonFormat.merge(sw.toString(), JsonFormatMessage1.newBuilder()));

        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        registry.add(JsonFormatProtos.extField);
        JsonFormatMessage1 decodedWithExtension = (JsonFormatMessage1) JsonFormat.merge(sw.toString(),
                JsonFormatMessage1.newBuilder(), registry);
        assertEquals(message, decodedWithExtension);
    }
}
