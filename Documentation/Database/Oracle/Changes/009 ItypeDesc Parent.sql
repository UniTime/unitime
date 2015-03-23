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

/*
 * Add column PARENT to table ITYPE_DESC
 */

 alter table ITYPE_DESC add PARENT number(2);
 
 alter table ITYPE_DESC add constraint FK_ITYPE_PARENT foreign key (PARENT) references ITYPE_DESC(ITYPE);
 
/*
 * Set parent itype to for all non-basic itypes to the appropriate basic itype
 */
 
 update ITYPE_DESC x set x.PARENT=
 	(select max(i.ITYPE) from ITYPE_DESC i where i.ITYPE<x.ITYPE and i.BASIC=1)
 	where x.BASIC=0;
 	
/*
 * Increase database version
 */

insert into APPLICATION_CONFIG (NAME,VALUE,DESCRIPTION)
	values ('tmtbl.db.version','9','Timetabling database version (please do not change -- this key is used by automatic database update)'); 

commit;
