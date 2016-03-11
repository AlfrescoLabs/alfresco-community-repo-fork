 
package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * 
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Cutoff indicator
 *
 * @author Roy Wetherall
 */
public class CutoffEvaluator extends BaseEvaluator
{
    private boolean isCutoff = true;

    public void setCutoff(boolean isCutoff)
    {
        this.isCutoff = isCutoff;
    }

    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        return (dispositionService.isDisposableItemCutoff(nodeRef) == isCutoff);
    }
}
