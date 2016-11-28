/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.model.site;

import static org.alfresco.rest.rm.community.model.site.RMSiteFields.COMPLIANCE;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.model.RestSiteModel;

/**
 * POJO for RM Site component
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class RMSite extends RestSiteModel
{
    @JsonProperty (value = COMPLIANCE,required = true)
    private RMSiteCompliance compliance;

    /**
     * Helper constructor to create RM Site object using
     *
     * @param title
     * @param description
     * @param compliance
     */
    public RMSite(String title, String description, RMSiteCompliance compliance)
    {
        this.title=title;
        this.description=description;
        this.compliance=compliance;
    }

    /**
     * Helper constructor for creating the RM Site
     */
    public RMSite() { }

    /**
     * Helper constructor to create RM Site object using
     *
     * @param compliance RM Site Compliance
     */
    public RMSite(RMSiteCompliance compliance)
    {
        super();
        this.compliance = compliance;
    }

    /**
     * Helper method to set RM site compliance
     *
     * @param compliance {@link RMSiteCompliance} the compliance to set
     */
    public void setCompliance(RMSiteCompliance compliance)
    {
        this.compliance = compliance;
    }

    /**
     * Helper method to get RM site compliance
     *
     * @return compliance the RM Site compliance to get
     */
    public RMSiteCompliance getCompliance()
    {
        return compliance;
    }

}
