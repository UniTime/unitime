/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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
