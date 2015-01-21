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
@SerializeWith(XAcademicAreaCode.XAcademicAreaCodeSerializer.class)
public class XAcademicAreaCode implements Serializable, Externalizable {
    private static final long serialVersionUID = 1L;
	private String iArea, iCode;
	
	public XAcademicAreaCode() {}
	
	public XAcademicAreaCode(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}

    public XAcademicAreaCode(String area, String code) {
        iArea = area;
        iCode = code;
    }

    /** Academic area */
    public String getArea() {
        return iArea;
    }

    /** Code */
    public String getCode() {
        return iCode;
    }

    @Override
    public int hashCode() {
        return (iArea + ":" + iCode).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XAcademicAreaCode))
            return false;
        XAcademicAreaCode aac = (XAcademicAreaCode) o;
        return ToolBox.equals(aac.getArea(), getArea()) && ToolBox.equals(aac.getCode(), getCode());
    }

    @Override
    public String toString() {
        return getArea() + ":" + getCode();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iArea = (String)in.readObject();
		iCode = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(iArea);
		out.writeObject(iCode);
	}
	
	public static class XAcademicAreaCodeSerializer implements Externalizer<XAcademicAreaCode> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XAcademicAreaCode object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XAcademicAreaCode readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XAcademicAreaCode(input);
		}
	}
}
