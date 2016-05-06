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
package org.unitime.timetable.solver.remote;

import java.io.File;
import java.io.FileFilter;

import org.unitime.timetable.model.SolverParameterGroup.SolverType;

/**
 * @author Tomas Muller
 */
public class BackupFileFilter implements FileFilter {
	public static String sXmlExtension = ".backup.xml";
	private SolverType iType;
	
	public BackupFileFilter(SolverType type) {
		iType = type;
	}

	public boolean accept(File file) {
		if (iType != null && !file.getName().startsWith(iType.getPrefix())) return false;
		return file.getName().endsWith(sXmlExtension);
	}

	public String getUser(File file) {
		if (accept(file)) {
			String name = file.getName();
			if (iType != null) name = name.substring(iType.getPrefix().length());
			if (name.endsWith(sXmlExtension)) name = name.substring(0, name.length() - sXmlExtension.length());
			return name;
		} else {
			return null;
		}
	}
}
