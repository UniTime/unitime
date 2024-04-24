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
package org.unitime.timetable.solver.jgroups;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.jgroups.Address;
import org.jgroups.BytesMessage;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

/**
 * A special version of the {@link RpcDispatcher} to avoid an endless loop when
 * a message fails to be parsed (see JGRP-2790 issue). All RPC messages are sent
 * as {@link BytesMessage} to avoid an issue when any of the arguments cannot be parsed.
 * Moreover, {@link UniTimeRpcDispatcher#callRemoteMethod(Address, MethodCall, RequestOptions)}
 * will also ensure that the serialization error gets passed back to the client when it
 * happens on the response message.
 * 
 * @author Tomas Muller
 */
public class UniTimeRpcDispatcher extends RpcDispatcher {
	
	public UniTimeRpcDispatcher(JChannel channel, Object server_obj) {
		super(channel, server_obj);
		try {
			channel.getProtocolStack().getTransport().getMessageFactory().register((short)99, RpcMessage::new);
		} catch (IllegalArgumentException e) {
			// ignore duplicate registrations
		}
	}

	@Override
	public <T> RspList<T> callRemoteMethods(Collection<Address> dests, MethodCall method_call, RequestOptions opts) throws Exception {
		if (dests != null && dests.isEmpty()) { // don't send if dest list is empty
			log.trace("destination list of %s() is empty: no need to send message", method_call.getMethodName());
			return empty_rsplist;
		}
		// serialize immediately, not during transport
		Message msg=new BytesMessage(null, method_call);
		RspList<T> retval=super.castMessage(dests, msg, opts);
		if(log.isTraceEnabled())
			log.trace("dests=%s, method_call=%s, options=%s, responses: %s", dests, method_call, opts, retval);
		return retval;
	}
	
	@Override
    public <T> CompletableFuture<RspList<T>> callRemoteMethodsWithFuture(Collection<Address> dests, MethodCall call, RequestOptions options) throws Exception {
		if (dests != null && dests.isEmpty()) { // don't send if dest list is empty
			log.trace("destination list of %s() is empty: no need to send message", call.getMethodName());
			return CompletableFuture.completedFuture(empty_rsplist);
		}
		// serialize immediately, not during transport
		Message msg=new BytesMessage(null, call);
		CompletableFuture<RspList<T>> retval=super.castMessageWithFuture(dests, msg, options);
		if(log.isTraceEnabled())
			log.trace("dests=%s, method_call=%s, options=%s", dests, call, options);
		return retval;
	}
	
	@Override
	public <T> T callRemoteMethod(Address dest, MethodCall call, RequestOptions options) throws Exception {
		// serialize immediately, not during transport
		// also pass serialization errors back to the client
        Message req=new RpcMessage(dest, call);
        T retval=super.sendMessage(req, options);
        if(log.isTraceEnabled())
            log.trace("dest=%s, method_call=%s, options=%s, retval: %s", dest, call, options, retval);
        if (retval != null && retval instanceof InvocationTargetException)
        	throw (InvocationTargetException)retval;
        return retval;
    }
	
	@Override
	public <T> CompletableFuture<T> callRemoteMethodWithFuture(Address dest, MethodCall call, RequestOptions opts) throws Exception {
		if(log.isTraceEnabled())
			log.trace("dest=%s, method_call=%s, options=%s", dest, call, opts);
		// serialize immediately, not during transport
        Message msg=new BytesMessage(dest, call);
        return super.sendMessageWithFuture(msg, opts);
    }
	
	@Override
	public Object handle(Message req) throws Exception {
		try {
			return super.handle(req);
		} catch (ClassCastException e) {
			// check if the request message could not be parsed -> pass the parse error instead
			Object method_call = req.getObject();
			if (method_call instanceof InvocationTargetException)
				throw (InvocationTargetException) method_call;
			throw e;
		}
    }
	
	public static class RpcMessage extends BytesMessage {
		RpcMessage() {
			super();
		}
		
		RpcMessage(Address dest, MethodCall call) {
			super(dest, call);
		}
		
		public Supplier<Message> create() { return RpcMessage::new; }
		public short getType() { return 99; }

		@Override
		public <T extends Object> T getObject(ClassLoader loader) {
			if(array == null)
	            return null;
	        try {
	            return isFlagSet(Flag.SERIALIZED)? Util.objectFromByteBuffer(array, offset, length, loader) : (T)getArray();
	        } catch (Exception e) {
	        	// message cannot be parsed > pretend the serialization error is the message
	        	// this is to ensure that the serialization error gets returned back to the client
	        	// and not just thrown in RequestCorrelator.handleResponse(..) and caught by UNICAST3.deliverMessage(..)
	        	return (T)new InvocationTargetException(e);
	        }
		}
	}
}
