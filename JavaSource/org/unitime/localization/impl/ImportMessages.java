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
			
			out.println("# Licensed to The Apereo Foundation under one or more contributor license");
			out.println("# agreements. See the NOTICE file distributed with this work for");
			out.println("# additional information regarding copyright ownership.");
			out.println("#");
			out.println("# The Apereo Foundation licenses this file to you under the Apache License,");
			out.println("# Version 2.0 (the \"License\"); you may not use this file except in");
			out.println("# compliance with the License. You may obtain a copy of the License at:");
			out.println("#");
			out.println("# http://www.apache.org/licenses/LICENSE-2.0");
			out.println("#");
			out.println("# Unless required by applicable law or agreed to in writing, software");
			out.println("# distributed under the License is distributed on an \"AS IS\" BASIS,");
			out.println("# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
			out.println("#");
			out.println("# See the License for the specific language governing permissions and");
			out.println("# limitations under the License.");
			out.println("#");
			
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
