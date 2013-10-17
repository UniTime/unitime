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
import java.util.TreeSet;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.SerializeWith;

/**
 * @author Tomas Muller
 */
@SerializeWith(XTreeSet.XTreeSetSerializer.class)
public class XTreeSet<T extends Externalizable> extends TreeSet<T> implements Externalizable {
	private static final long serialVersionUID = 1L;
	private Externalizer<T> iExternalizer;
	
	public XTreeSet(Externalizer<T> externalizer) {
		super();
		iExternalizer = externalizer;
	}
	
	public XTreeSet(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int count = in.readInt();
		iExternalizer = (Externalizer<T>)in.readObject();
		clear();
		for (int i = 0; i < count; i++)
			add(iExternalizer.readObject(in));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(size());
		out.writeObject(iExternalizer);
		for (T t: this)
			t.writeExternal(out);
	}

	public static class XTreeSetSerializer implements Externalizer<XTreeSet<?>> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XTreeSet<?> object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XTreeSet<?> readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XTreeSet(input);
		}		
	}
}
