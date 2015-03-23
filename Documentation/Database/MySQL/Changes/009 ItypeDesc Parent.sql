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

 alter table `timetable`.`itype_desc` add `parent` int(2);
 
 alter table `timetable`.`itype_desc` add constraint `fk_itype_parent` foreign key `fk_itype_parent` (`parent`) references `itype_desc`(`itype`);
 
/*
 * Set parent itype to for all non-basic itypes to the appropriate basic itype
 */

 create table `timetable`.`itype_dummy` as
 	(select i.`itype`, max(x.`itype`) as `parent` from `timetable`.`itype_desc` i, `timetable`.`itype_desc` x where i.`basic`=0 and x.`basic`=1 and x.`itype`<i.`itype` group by i.`itype`);

update `timetable`.`itype_desc` i set i.`parent`=(select x.`parent` from `timetable`.`itype_dummy` x where x.`itype`=i.`itype`);

drop table `timetable`.`itype_dummy`;

/*
 * Increase database version
 */

insert into `timetable`.`application_config` (name,value,description)
	values('tmtbl.db.version','9','Timetabling database version (please do not change -- this key is used by automatic database update)'); 
			
commit;
