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
package org.unitime.commons;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

/**
 * Inspired by the sample service provider that implements a hierarchical namespace in memory:
 * http://docs.oracle.com/javase/jndi/tutorial/provider/basics/names.html
 *
 * @author Tomas Muller
 */
public class LocalContext implements Context, NameParser, InitialContextFactory, InitialContextFactoryBuilder {
	protected Hashtable iEnv;
	protected Hashtable iBindings = new Hashtable();
	protected LocalContext iParent = null;
	protected String iName = null;
	private static final Properties sSyntax = new Properties();
	static {
		sSyntax.put("jndi.syntax.direction", "right_to_left");
		sSyntax.put("jndi.syntax.separator", ".");
		sSyntax.put("jndi.syntax.ignorecase", "false");
		sSyntax.put("jndi.syntax.escape", "\\");
		sSyntax.put("jndi.syntax.beginquote", "'");
	}

	public LocalContext(Hashtable env) {
		iEnv = (env != null ? (Hashtable) env.clone() : null);
	}

	protected LocalContext(LocalContext parent, String name, Hashtable env, Hashtable bindings) {
		this(env);
		iParent = parent;
		iName = name;
		iBindings = (Hashtable) bindings.clone();
	}

	protected Context createCtx(LocalContext parent, String name, Hashtable env) {
		return new LocalContext(parent, name, env, new Hashtable());
	}

	protected Context cloneCtx() {
		return new LocalContext(iParent, iName, iEnv, iBindings);
	}

	protected Name getMyComponents(Name name) throws NamingException {
		if (name instanceof CompositeName) {
			if (name.size() > 1)
				throw new InvalidNameException(name.toString() + " has more components than namespace can handle");
			return parse(name.get(0));
		} else {
			return name;
		}
	}

	@Override
	public Object lookup(String name) throws NamingException {
		return lookup(new CompositeName(name));
	}

	@Override
	public Object lookup(Name name) throws NamingException {
		if (name.isEmpty())
			return cloneCtx();

		Name nm = getMyComponents(name);
		String atom = nm.get(0);
		Object inter = iBindings.get(atom);

		if (nm.size() == 1) {
			if (inter == null)
				throw new NameNotFoundException(name + " not found");

			try {
				return NamingManager.getObjectInstance(inter, new CompositeName().add(atom), this, iEnv);
			} catch (Exception e) {
				NamingException ne = new NamingException("getObjectInstance failed");
				ne.setRootCause(e);
				throw ne;
			}
		} else {
			if (!(inter instanceof Context))
				throw new NotContextException(atom + " does not name a context");

			return ((Context) inter).lookup(nm.getSuffix(1));
		}
	}

	@Override
	public void bind(String name, Object obj) throws NamingException {
		bind(new CompositeName(name), obj);
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException {
		if (name.isEmpty()) {
			throw new InvalidNameException("Cannot bind empty name");
		}

		Name nm = getMyComponents(name);
		String atom = nm.get(0);
		Object inter = iBindings.get(atom);

		if (nm.size() == 1) {
			if (inter != null)
				throw new NameAlreadyBoundException("Use rebind to override");

			obj = NamingManager.getStateToBind(obj, new CompositeName().add(atom), this, iEnv);

			iBindings.put(atom, obj);
		} else {
			if (!(inter instanceof Context))
				throw new NotContextException(atom + " does not name a context");

			((Context) inter).bind(nm.getSuffix(1), obj);
		}
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException {
		rebind(new CompositeName(name), obj);
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		if (name.isEmpty())
			throw new InvalidNameException("Cannot bind empty name");

		Name nm = getMyComponents(name);
		String atom = nm.get(0);

		if (nm.size() == 1) {
			obj = NamingManager.getStateToBind(obj, new CompositeName().add(atom), this, iEnv);

			iBindings.put(atom, obj);
		} else {
			Object inter = iBindings.get(atom);
			
			if (!(inter instanceof Context))
				throw new NotContextException(atom + " does not name a context");

			((Context) inter).rebind(nm.getSuffix(1), obj);
		}
	}

	@Override
	public void unbind(String name) throws NamingException {
		unbind(new CompositeName(name));
	}

	@Override
	public void unbind(Name name) throws NamingException {
		if (name.isEmpty())
			throw new InvalidNameException("Cannot unbind empty name");

		Name nm = getMyComponents(name);
		String atom = nm.get(0);

		if (nm.size() == 1) {
			iBindings.remove(atom);
		} else {
			Object inter = iBindings.get(atom);
			
			if (!(inter instanceof Context))
				throw new NotContextException(atom + " does not name a context");

			((Context) inter).unbind(nm.getSuffix(1));
		}
	}

	@Override
	public void rename(String oldname, String newname) throws NamingException {
		rename(new CompositeName(oldname), new CompositeName(newname));
	}

	@Override
	public void rename(Name oldname, Name newname) throws NamingException {
		if (oldname.isEmpty() || newname.isEmpty())
			throw new InvalidNameException("Cannot rename empty name");

		Name oldnm = getMyComponents(oldname);
		Name newnm = getMyComponents(newname);

		if (oldnm.size() != newnm.size())
			throw new OperationNotSupportedException("Do not support rename across different contexts");

		String oldatom = oldnm.get(0);
		String newatom = newnm.get(0);

		if (oldnm.size() == 1) {
			if (iBindings.get(newatom) != null)
				throw new NameAlreadyBoundException(newname.toString() + " is already bound");

			Object oldBinding = iBindings.remove(oldatom);
			if (oldBinding == null)
				throw new NameNotFoundException(oldname.toString() + " not bound");

			iBindings.put(newatom, oldBinding);
		} else {
			if (!oldatom.equals(newatom))
				throw new OperationNotSupportedException("Do not support rename across different contexts");

			Object inter = iBindings.get(oldatom);
			
			if (!(inter instanceof Context))
				throw new NotContextException(oldatom + " does not name a context");

			((Context) inter).rename(oldnm.getSuffix(1), newnm.getSuffix(1));
		}
	}

	@Override
	public NamingEnumeration list(String name) throws NamingException {
		return list(new CompositeName(name));
	}
	
	@Override
	public NamingEnumeration list(Name name) throws NamingException {
		if (name.isEmpty())
			return new ListOfNames(iBindings.keys());

		Object target = lookup(name);
		if (target instanceof Context)
			return ((Context) target).list("");


		throw new NotContextException(name + " cannot be listed");
	}

	@Override
	public NamingEnumeration listBindings(String name) throws NamingException {
		return listBindings(new CompositeName(name));
	}

	@Override
	public NamingEnumeration listBindings(Name name) throws NamingException {
		if (name.isEmpty())
			return new ListOfBindings(iBindings.keys());

		Object target = lookup(name);
		if (target instanceof Context)
			return ((Context) target).listBindings("");

		throw new NotContextException(name + " cannot be listed");
	}

	@Override
	public void destroySubcontext(String name) throws NamingException {
		destroySubcontext(new CompositeName(name));
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException {
		if (name.isEmpty())
			throw new InvalidNameException("Cannot destroy context using empty name");

		unbind(name);
	}

	@Override
	public Context createSubcontext(String name) throws NamingException {
		return createSubcontext(new CompositeName(name));
	}

	@Override
	public Context createSubcontext(Name name) throws NamingException {
		if (name.isEmpty())
			throw new InvalidNameException("Cannot bind empty name");

		Name nm = getMyComponents(name);
		String atom = nm.get(0);
		Object inter = iBindings.get(atom);

		if (nm.size() == 1) {
			if (inter != null)
				throw new NameAlreadyBoundException("Use rebind to override");

			Context child = createCtx(this, atom, iEnv);

			iBindings.put(atom, child);

			return child;
		} else {
			if (!(inter instanceof Context))
				throw new NotContextException(atom + " does not name a context");

			return ((Context) inter).createSubcontext(nm.getSuffix(1));
		}
	}

	@Override
	public Object lookupLink(String name) throws NamingException {
		return lookupLink(new CompositeName(name));
	}

	@Override
	public Object lookupLink(Name name) throws NamingException {
		return lookup(name);
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException {
		return getNameParser(new CompositeName(name));
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		Object obj = lookup(name);
		
		if (obj instanceof Context)
			((Context) obj).close();

		return this;
	}

	@Override
	public String composeName(String name, String prefix) throws NamingException {
		Name result = composeName(new CompositeName(name), new CompositeName(prefix));
		return result.toString();
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException {
		Name result;

		if (!(name instanceof CompositeName) && !(prefix instanceof CompositeName)) {
			result = (Name) (prefix.clone());
			result.addAll(name);
			return new CompositeName().add(result.toString());
		}

		throw new OperationNotSupportedException("Do not support composing composite names");
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal) throws NamingException {
		if (iEnv == null)
			iEnv = new Hashtable(5, 0.75f);

		return iEnv.put(propName, propVal);
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException {
		if (iEnv == null)
			return null;

		return iEnv.remove(propName);
	}

	@Override
	public Hashtable getEnvironment() throws NamingException {
		if (iEnv == null)
			return new Hashtable(3, 0.75f);

		return (Hashtable) iEnv.clone();
	}

	@Override
	public String getNameInNamespace() throws NamingException {
		LocalContext ancestor = iParent;

		if (ancestor == null) return "";

		Name name = parse("");
		name.add(iName);

		while (ancestor != null && ancestor.iName != null) {
			name.add(0, ancestor.iName);
			ancestor = ancestor.iParent;
		}

		return name.toString();
	}

	@Override
	public String toString() {
		if (iName != null) {
			return iName;
		} else {
			return "ROOT CONTEXT";
		}
	}

	@Override
	public void close() throws NamingException {
	}

	// Class for enumerating name/class pairs
	class ListOfNames implements NamingEnumeration {
		protected Enumeration iNames;

		ListOfNames(Enumeration names) {
			iNames = names;
		}

		public boolean hasMoreElements() {
			try {
				return hasMore();
			} catch (NamingException e) {
				return false;
			}
		}

		public boolean hasMore() throws NamingException {
			return iNames.hasMoreElements();
		}

		public Object next() throws NamingException {
			String name = (String) iNames.nextElement();
			String className = iBindings.get(name).getClass().getName();
			return new NameClassPair(name, className);
		}

		public Object nextElement() {
			try {
				return next();
			} catch (NamingException e) {
				throw new NoSuchElementException(e.toString());
			}
		}

		public void close() {
		}
	}

	// Class for enumerating bindings
	class ListOfBindings extends ListOfNames {

		ListOfBindings(Enumeration names) {
			super(names);
		}

		public Object next() throws NamingException {
			String name = (String) iNames.nextElement();
			Object obj = iBindings.get(name);

			try {
				obj = NamingManager.getObjectInstance(obj, new CompositeName().add(name), LocalContext.this, LocalContext.this.iEnv);
			} catch (Exception e) {
				NamingException ne = new NamingException("getObjectInstance failed");
				ne.setRootCause(e);
				throw ne;
			}

			return new Binding(name, obj);
		}
	}

	@Override
	public Name parse(String name) throws NamingException {
		return new CompoundName(name, sSyntax);
	}

	@Override
	public InitialContextFactory createInitialContextFactory(Hashtable env) throws NamingException {
		return new LocalContext(env);
	}

	@Override
	public Context getInitialContext(Hashtable env) throws NamingException {
		return new LocalContext(env);
	}

}