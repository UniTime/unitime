/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.command.generator;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.GeneratorContextExt;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.dev.javac.rebind.RebindResult;
import com.google.gwt.user.rebind.rpc.ProxyCreator;
import com.google.gwt.user.rebind.rpc.ServiceInterfaceProxyGenerator;

public class GwtRpcProxyGenerator extends ServiceInterfaceProxyGenerator {
	private GeneratorContext iGeneratorContext = null;
	
	public GwtRpcProxyGenerator() {
		super();
	}
	
	@Override
    public RebindResult generateIncrementally(TreeLogger logger, GeneratorContextExt ctx, String requestedClass) throws UnableToCompleteException {
		if (iGeneratorContext == null) iGeneratorContext = ctx;
		return super.generateIncrementally(logger, ctx, requestedClass);
    }
	
	@Override
	protected ProxyCreator createProxyCreator(JClassType remoteService) {
		return new GwtRpcProxyCreator(remoteService, iGeneratorContext);
	}
}
