<#-- 
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
 -->
<span<#rt/>
 name='UniTimeGWT:CourseNumberSuggestBox'<#rt/>
<#if parameters.get("configuration")?has_content>
 configuration="${parameters.get("configuration")}"<#rt/>
</#if>
<#if parameters.get("outerStyle")?has_content>
 style="${parameters.get("outerStyle")}"<#rt/>
</#if>
<#if parameters.disabled!false>
 disabled="disabled"<#rt/>
</#if>
<#if fieldErrors?? && fieldErrors.containsKey(parameters.name)><#assign eValue = fieldErrors.get(parameters.name)>
 error="${eValue[0]!}"<#rt/>
</#if>
<#if parameters.onchange??>
  onchange="${parameters.onchange}"<#rt/>
</#if>
><#rt/>
<input<#rt/>
 type="${(parameters.type!"text")}"<#rt/>
 name="${(parameters.name!"")}"<#rt/>
<#if parameters.get("size")?has_content>
 size="${parameters.get("size")}"<#rt/>
</#if>
<#if parameters.maxlength?has_content>
 maxlength="${parameters.maxlength}"<#rt/>
</#if>
<#if parameters.nameValue??>
 value="${parameters.nameValue}"<#rt/>
</#if>
<#if parameters.readonly!false>
 readonly="readonly"<#rt/>
</#if>
<#if parameters.tabindex?has_content>
 tabindex="${parameters.tabindex}"<#rt/>
</#if>
<#if parameters.id?has_content>
 id="${parameters.id}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/${parameters.expandTheme}/css.ftl" />
<#if parameters.title?has_content>
 title="${parameters.title}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/${parameters.expandTheme}/common-attributes.ftl" />
<#include "/${parameters.templateDir}/${parameters.expandTheme}/dynamic-attributes.ftl" />
/>
</span><#rt/>