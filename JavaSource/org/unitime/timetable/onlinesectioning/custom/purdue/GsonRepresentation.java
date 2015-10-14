/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class GsonRepresentation<T> extends WriterRepresentation {
    private GsonBuilder iBuilder = null;
    private Representation iRepresentation = null;
    private T iObject = null;
    private Type iObjectType = null;
    private Class<T> iObjectClass = null;

    public GsonRepresentation(Representation representation, Class<T> objectClass) {
        super(representation.getMediaType());
        iObject = null;
        iObjectClass = objectClass;
        iRepresentation = representation;
    }
    
    public GsonRepresentation(Representation representation, Type objectType) {
        super(representation.getMediaType());
        iObject = null;
        iObjectType = objectType;
        iRepresentation = representation;
    }

    public GsonRepresentation(T object) {
        super(MediaType.APPLICATION_JSON);
        iObject = object;
        iObjectClass = ((Class<T>) ((object == null) ? null : object.getClass()));
    }

    public GsonBuilder getBuilder() {
        if (iBuilder == null) {
        	iBuilder = new GsonBuilder()
        	.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
    			@Override
    			public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
    				return new JsonPrimitive(src.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    			}
    		})
    		.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
    			@Override
    			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    				return new DateTime(json.getAsJsonPrimitive().getAsString(), DateTimeZone.UTC);
    			}
    		});
        }
        return iBuilder;
    }

    public T getObject() throws IOException {
        if (iObject == null && iRepresentation != null && iRepresentation.isAvailable()) {
        	JsonReader reader = new JsonReader(iRepresentation.getReader());
        	try {
        		if (iObjectType != null)
        			return getBuilder().create().fromJson(reader, iObjectType);
        		else
        			return getBuilder().create().fromJson(reader, iObjectClass);
        	} finally {
    			reader.close();
    			iRepresentation.release();
        	}
        }
        return iObject;
    }

    public Class<T> getObjectClass() {
        return iObjectClass;
    }
    
    public void setObjectClass(Class<T> objectClass) {
        iObjectClass = objectClass;
    }

    public Type getType() {
    	return iObjectType;
    }
    
    public void setType(Type type) {
    	iObjectType = type;
    }

    public void setObject(T object) {
    	iObject = object;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (iRepresentation != null) {
        	iRepresentation.write(writer);
        } else {
        	if (iObjectType != null)
        		getBuilder().create().toJson(iObject, iObjectType, new JsonWriter(writer));
        	else
        		getBuilder().create().toJson(iObject, iObjectClass, new JsonWriter(writer));
        }
    }
}
