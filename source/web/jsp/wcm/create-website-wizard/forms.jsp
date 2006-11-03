<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid columns="1" cellpadding="2" cellpadding="2" width="100%">
   <%-- Form selection list - scrollable DIV area --%>
   <h:outputText styleClass="mainSubText" value="#{msg.website_select_form}:" />
   <h:panelGroup>
      <f:verbatim><div style="height:108px;*height:112px;width:300px;overflow:auto" class='selectListTable'></f:verbatim>
      <a:selectList id="form-list" activeSelect="true" style="width:276px" itemStyleClass="selectListItem">
         <a:listItems value="#{WizardManager.bean.formsList}" />
         <h:commandButton value="#{msg.add_to_list_button}" styleClass="dialogControls" actionListener="#{WizardManager.bean.addForm}" />
      </a:selectList>
      <f:verbatim></div></f:verbatim>
   </h:panelGroup>
   
   <%-- Selected Form table, with configuration buttons and info text --%>
   <h:outputText styleClass="mainSubText" style="padding-top:8px" value="#{msg.website_selected_forms}:" />
   <h:dataTable value="#{WizardManager.bean.formsDataModel}" var="row" 
                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                styleClass="selectedItems" headerClass="selectedItemsHeader"
                cellspacing="0" cellpadding="4" 
                rendered="#{WizardManager.bean.formsDataModel.rowCount != 0}">
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.title}" />
         </f:facet>
         <h:outputText value="#{row.title}" />
      </h:column>
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.details}" />
         </f:facet>
         <h:outputText value="#{row.details}" />
      </h:column>
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.configure}" />
         </f:facet>
         <h:commandButton value="#{msg.form_template_details}" style="margin:2px" styleClass="dialogControls" action="dialog:formTemplateDetails" actionListener="#{WizardManager.bean.setupFormAction}" />
         <h:commandButton value="#{msg.form_template_conf_workflow}" style="margin:2px" styleClass="dialogControls" action="dialog:formTemplateWorkflow" actionListener="#{WizardManager.bean.setupFormAction}" disabled="#{row.workflow == null}" />
         <h:commandButton value="#{msg.form_template_select_templates}" style="margin:2px" styleClass="dialogControls" action="dialog:formTemplateTemplates" actionListener="#{WizardManager.bean.setupFormAction}" />
      </h:column>
      <h:column>
         <a:actionLink actionListener="#{WizardManager.bean.removeForm}" image="/images/icons/delete.gif"
                       value="#{msg.remove}" showLink="false" style="padding-left:6px" />
      </h:column>
   </h:dataTable>
   
   <a:panel id="no-items" rendered="#{WizardManager.bean.formsDataModel.rowCount == 0}">
      <h:panelGrid columns="1" cellpadding="2" styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
         <h:outputText id="no-items-name" value="#{msg.name}" />
         <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
   </a:panel>
</h:panelGrid>
