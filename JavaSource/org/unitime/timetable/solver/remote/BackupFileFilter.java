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

import org.unitime.timetable.model.SolverParameterGroup;

/**
 * @author Tomas Muller
 */
public class BackupFileFilter implements FileFilter {
	public static String sXmlExtension = ".backup.xml";
	public static String sPropertiesExtension = ".backup.properties";
	private boolean iAcceptXml = false;
	private boolean iAcceptProperties = false;
	private int iType = 0;
	public BackupFileFilter(boolean acceptXml, boolean acceptProperties, int type) {
		iAcceptXml = acceptXml;
		iAcceptProperties = acceptProperties;
		iType = type;
	}
	public BackupFileFilter(boolean acceptXml, boolean acceptProperties) {
		this(acceptXml, acceptProperties, -1);
	}
	public boolean accept(File file) {
		switch (iType) {
		case SolverParameterGroup.sTypeCourse:
			if (file.getName().startsWith("exam_") || file.getName().startsWith("sct_")) return false;
			break;
		case SolverParameterGroup.sTypeExam:
			if (!file.getName().startsWith("exam_")) return false;
			break;
		case SolverParameterGroup.sTypeStudent:
			if (!file.getName().startsWith("sct_")) return false;
			break;
		}
		return ((iAcceptXml && file.getName().endsWith(sXmlExtension)) || (iAcceptProperties && file.getName().endsWith(sPropertiesExtension)));
	}
}
