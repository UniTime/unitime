/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.SerializeWith;

import net.sf.cpsolver.ifs.util.ToolBox;

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
