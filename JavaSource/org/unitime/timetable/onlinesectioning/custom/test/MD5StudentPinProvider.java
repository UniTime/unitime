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
package org.unitime.timetable.onlinesectioning.custom.test;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.StudentPinsProvider;
import org.unitime.timetable.onlinesectioning.model.XStudentId;

public class MD5StudentPinProvider implements StudentPinsProvider {

	@Override
	public String retriveStudentPin(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId student) throws SectioningException {
		try {
			if (student.getExternalId() == null || student.getExternalId().isEmpty()) return null;
			MessageDigest md5 = MessageDigest.getInstance("MD5");
		    String hash = new BigInteger(md5.digest(student.getExternalId().getBytes())).toString(36).toUpperCase();
		    if (hash.length() > 7)
		    	return hash.substring(0, 7);
		    else
		    	return hash;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

}
