/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
