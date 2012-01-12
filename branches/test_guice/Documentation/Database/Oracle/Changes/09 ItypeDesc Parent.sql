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
