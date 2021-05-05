package org.cc.common.exception;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.cc.common.model.MyOauthException;

import java.io.IOException;

public class UserOAuth2ExceptionSerializer extends StdSerializer<MyOauthException> {

    protected UserOAuth2ExceptionSerializer() {
        super(MyOauthException.class);
    }

    @Override
    public void serialize(MyOauthException value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("code", value.getCode());
        gen.writeStringField("message", value.getMessage());
        gen.writeEndObject();
    }
}
