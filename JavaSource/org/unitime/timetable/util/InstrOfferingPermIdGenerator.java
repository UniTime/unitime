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
package org.unitime.timetable.util;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.type.Type;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.commons.hibernate.util.HibernateUtil;

/**
 * 
 * @author Tomas Muller
 *
 */
public class InstrOfferingPermIdGenerator {
    private static UniqueIdGenerator sGenerator = null;
    
    protected static String sSequence = "instr_offr_permid_seq";
    
    public static UniqueIdGenerator getGenerator() throws HibernateException {
        try {
            if (sGenerator!=null) return sGenerator;
            UniqueIdGenerator idGen = new UniqueIdGenerator();
            Type type = ((MetadataImplementor)HibernateUtil.getHibernateContext().getMetadata()).getTypeConfiguration().getBasicTypeForJavaType(Long.class);
            Properties params = new Properties();
            params.put(SequenceStyleGenerator.SEQUENCE_PARAM, sSequence);
            idGen.configure(type, params, HibernateUtil.getHibernateContext().getServiceRegistry());
            idGen.registerExportables(
            		HibernateUtil.getHibernateContext().getMetadata().getDatabase()
            		);
            idGen.initialize(
            		((SessionFactoryImpl)HibernateUtil.getHibernateContext().getSessionFactory()).getSqlStringGenerationContext()
            		);
            sGenerator = idGen;
            return sGenerator;
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }
}
