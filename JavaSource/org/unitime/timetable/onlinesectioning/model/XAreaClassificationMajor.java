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

import org.cpsolver.ifs.util.ToolBox;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;


/**
 * @author Tomas Muller
 */
@SerializeWith(XAreaClassificationMajor.XAreaClassificationMajorSerializer.class)
public class XAreaClassificationMajor implements Serializable, Externalizable, Comparable<XAreaClassificationMajor> {
    private static final long serialVersionUID = 1L;
	private String iArea, iClassification, iMajor;
	
	public XAreaClassificationMajor() {}
	
	public XAreaClassificationMajor(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}

    public XAreaClassificationMajor(String area, String classification, String major) {
        iArea = area;
        iClassification = classification;
        iMajor = major;
    }

    /** Academic area */
    public String getArea() {
        return iArea;
    }

    /** Classification */
    public String getClassification() {
        return iClassification;
    }
    
    /** Major */
    public String getMajor() {
        return iMajor;
    }

    @Override
    public int hashCode() {
        return (iArea + ":" + iClassification + ":" + iMajor).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XAreaClassificationMajor))
            return false;
        XAreaClassificationMajor acm = (XAreaClassificationMajor) o;
        return ToolBox.equals(acm.getArea(), getArea()) && ToolBox.equals(acm.getClassification(), getClassification()) && ToolBox.equals(acm.getMajor(), getMajor());
    }

    @Override
    public String toString() {
        return getArea() + "/" + getMajor() + " " + getClassification();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iArea = (String)in.readObject();
		iClassification = (String)in.readObject();
		iMajor = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(iArea);
		out.writeObject(iClassification);
		out.writeObject(iMajor);
	}
	
	public static class XAreaClassificationMajorSerializer implements Externalizer<XAreaClassificationMajor> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XAreaClassificationMajor object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XAreaClassificationMajor readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XAreaClassificationMajor(input);
		}
	}

	@Override
	public int compareTo(XAreaClassificationMajor acm) {
		if (!getArea().equals(acm.getArea()))
			return getArea().compareTo(acm.getArea());
		if (!getClassification().equals(acm.getClassification()))
			return getClassification().compareTo(acm.getClassification());
		return getMajor().compareTo(acm.getMajor());
	}
}
