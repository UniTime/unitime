/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.localization.impl;

import java.io.File;
import java.io.PrintStream;

import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVLine;


/**
 * @author Tomas Muller, Zuzana Mullerova
 */
public class ImportMessages {
	
    private static final char[] hexChar = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    private static String unicodeEscape(String s) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < s.length(); i++) {
    	    char c = s.charAt(i);
    	    if ((c >> 7) > 0) {
    		sb.append("\\u");
    		sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
    		sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
    		sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
    		sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
    	    }
    	    else {
    		sb.append(c);
    	    }
    	}
    	return sb.toString();
    }
    
	public static void main(String[] args) {
		try {
			PrintStream out = new PrintStream(System.out, true, "UTF-8");
			
			out.println("# UniTime 3.5 (University Timetabling Application)");
			out.println("# Copyright (C) 2012 - 2014, UniTime LLC, and individual contributors");
			out.println("# as indicated by the @authors tag.");
			out.println("# ");
			out.println("# This program is free software; you can redistribute it and/or modify");
			out.println("# it under the terms of the GNU General Public License as published by");
			out.println("# the Free Software Foundation; either version 3 of the License, or");
			out.println("# (at your option) any later version.");
			out.println("# ");
			out.println("# This program is distributed in the hope that it will be useful,");
			out.println("# but WITHOUT ANY WARRANTY; without even the implied warranty of");
			out.println("# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
			out.println("# GNU General Public License for more details.");
			out.println("# ");
			out.println("# You should have received a copy of the GNU General Public License along");
			out.println("# with this program.  If not, see <http://www.gnu.org/licenses/>.");
			out.println("# ");
			
			CSVFile csv = new CSVFile(new File(System.getProperty("file", System.getProperty("user.home") + "/Downloads/UniTime Localization Czech.csv")));
			for (CSVLine line: csv.getLines()) {
				if (line.getFields().size() >= 2) {
					out.println();
					if (line.getFields().size() >= 3 && (!line.getField(2).isEmpty() || line.getField(1).isEmpty())) {
						out.println("# Default: " + unicodeEscape(line.getField(1).toString()));
						out.println(line.getField(0).toString() + "=" + unicodeEscape(line.getField(2).toString()).replace(":", "\\:"));
					} else {
						out.println("# Default: " + unicodeEscape(line.getField(1).toString()));
						out.println("# FIXME: Translate \"" + unicodeEscape(line.getField(1).toString()) + "\"");
						out.println("# " + line.getField(0).toString() + "=");
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
