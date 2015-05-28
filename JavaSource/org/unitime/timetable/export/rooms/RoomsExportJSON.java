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
package org.unitime.timetable.export.rooms;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.security.rights.Right;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:rooms.json")
public class RoomsExportJSON extends RoomsExporter {

	@Override
	public String reference() { return "rooms.json"; }
	
	@Override
	protected void print(ExportHelper helper, List<RoomDetailInterface> rooms, ExportContext context) throws IOException {
		if (checkRights(helper))
			helper.getSessionContext().hasPermission(Right.RoomsExportJson);
		
		helper.setup("application/json", reference(), false);
		
    	Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
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
    	
    	helper.getWriter().write(gson.toJson(rooms));
	}
}
