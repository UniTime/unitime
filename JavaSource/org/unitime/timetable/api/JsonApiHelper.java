/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.api;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unitime.timetable.security.SessionContext;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;

/**
 * @author Tomas Muller
 */
public class JsonApiHelper extends AbstractApiHelper {
	protected Gson iGson;
	
	public JsonApiHelper(HttpServletRequest request, HttpServletResponse response, SessionContext context) {
		super(request, response, context);
	}
			
	protected Gson createGson() {
		return new GsonBuilder()
		.registerTypeAdapter(java.sql.Timestamp.class, new JsonSerializer<java.sql.Timestamp>() {
			@Override
			public JsonElement serialize(java.sql.Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.registerTypeAdapter(java.sql.Date.class, new JsonSerializer<java.sql.Date>() {
			@Override
			public JsonElement serialize(java.sql.Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd").format(src));
			}
		})
		.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.setFieldNamingStrategy(new FieldNamingStrategy() {
			Pattern iPattern = Pattern.compile("i([A-Z])(.*)");
			@Override
			public String translateName(Field f) {
				Matcher matcher = iPattern.matcher(f.getName());
				if (matcher.matches())
					return matcher.group(1).toLowerCase() + matcher.group(2);
				else
					return f.getName();
			}
		})
		.setPrettyPrinting().create();
	}
	
	@Override
	public <P> P getRequest(Type requestType) throws IOException {
		if (iGson == null) iGson = createGson();
		JsonReader reader = new JsonReader(iRequest.getReader());
		try {
			return iGson.fromJson(reader, requestType);
		} finally {
			reader.close();
		}
	}
	
	@Override
	public <R> void setResponse(R response) throws IOException {
		if (iGson == null) iGson = createGson();
		iResponse.setContentType("application/json");
		iResponse.setCharacterEncoding("UTF-8");
		iResponse.setHeader("Pragma", "no-cache" );
		iResponse.addHeader("Cache-Control", "must-revalidate" );
		iResponse.addHeader("Cache-Control", "no-cache" );
		iResponse.addHeader("Cache-Control", "no-store" );
		iResponse.setDateHeader("Date", new Date().getTime());
		iResponse.setDateHeader("Expires", 0);
		iResponse.setHeader("Content-Disposition", "attachment; filename=\"response.json\"" );
		Writer writer = iResponse.getWriter();
		try {
			writer.write(iGson.toJson(response));
		} finally {
			writer.flush();
			writer.close();
		}
	}
}
