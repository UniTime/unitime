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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.CourseCreditUnitConfig;

/**
 * @author Tomas Muller
 */
@SerializeWith(XCredit.XCreditSerializer.class)
public class XCredit implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iId;
	private String iAbbreviation;
	private String iText;
	private float iMin = 0f;
	private float iMax = 0f;
	
	public XCredit() {}
	
	public XCredit(CourseCreditUnitConfig credit) {
		iId = credit.getUniqueId();
		iAbbreviation = credit.creditAbbv();
		iText = credit.creditText();
		iMin = credit.getMinCredit();
		iMax = credit.getMaxCredit();
	}
	
	public XCredit(String credit) {
    	int split = credit.indexOf('|');
    	if (split >= 0) {
    		iAbbreviation = credit.substring(0, split);
    		iText = credit.substring(split + 1);
    	} else {
    		iAbbreviation = credit;
    		iText = credit;
    	}
    	Matcher m = Pattern.compile("(^| )(\\d+\\.?\\d*)([,-]?(\\d+\\.?\\d*))?($| )").matcher(iAbbreviation);
    	if (m.find()) {
    		iMin = Float.parseFloat(m.group(2));
    		if (m.group(4) != null)
    			iMax = Float.parseFloat(m.group(4));
    		else
    			iMax = iMin;
    	}
	}
	
	public XCredit(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
	
	public Long getId() { return iId; }
	public String getAbbreviation() { return iAbbreviation; }
	public String getText() { return iText; }
	public Float getMinCredit() { return iMin; }
	public Float getMaxCredit() { return iMax; }

	@Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XCredit)) return false;
        return getAbbreviation().equals(((XCredit)o).getAbbreviation());
    }
    
    @Override
    public String toString() {
    	return getAbbreviation() + "|" + getText();
    }
    
    @Override
    public int hashCode() {
        return getAbbreviation().hashCode();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iId = in.readLong();
		if (iId < 0) iId = null;
		iAbbreviation = (String)in.readObject();
		iText = (String)in.readObject();
		iMin = in.readFloat();
		iMax = in.readFloat();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iId == null ? -1l : iId);
		out.writeObject(iAbbreviation);
		out.writeObject(iText);
		out.writeFloat(iMin);
		out.writeFloat(iMax);
	}
	
	public static class XCreditSerializer implements Externalizer<XCredit> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCredit object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCredit readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCredit(input);
		}
	}
	
	public static void main(String[] args) {
		String[] test = new String[] {
				"AH", "2", "1.5", "1.4 EQV", "1.4,2.5 EQV", "M/X 2-3 MS", "XX 2,3.5 PhD", "VR 1,2", "2-4.333 SH",
		};
		for (String s: test) {
			XCredit c = new XCredit(s);
			System.out.println(c.getAbbreviation() + ": " + c.getMinCredit() + " .. " + c.getMaxCredit());
		}
	}
}
