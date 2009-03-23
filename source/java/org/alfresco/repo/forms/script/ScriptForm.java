/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.script;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.jscript.ScriptableHashMap;

/**
 * Form JavaScript Object.
 * 
 * @author Neil Mc Erlean
 */
public class ScriptForm implements Serializable
{
    private static final long serialVersionUID = 579853076546002023L;

    private Form form;
    private Map<String, FieldDefinition> fieldDefinitionData;
    //TODO Consider caching

    /* default */ScriptForm(Form formObject)
    {
        this.form = formObject;
        
        fieldDefinitionData = new HashMap<String, FieldDefinition>();
        for (FieldDefinition fd : form.getFieldDefinitions()) {
            fieldDefinitionData.put(fd.getName(), fd);
        }
    }

    public String getItem()
    {
        return form.getItem();
    }

    public String getType()
    {
        return form.getType();
    }

    //TODO Wrap this type in a script type?
    public Collection<FieldGroup> getFieldGroups()
    {
        return form.getFieldGroups();
    }

    public FieldDefinition[] getFieldDefinitions()
    {
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        if (fieldDefs == null)
        {
            fieldDefs = Collections.emptyList();
        }
        return fieldDefs.toArray(new FieldDefinition[fieldDefs.size()]);
    }
    
    public ScriptableHashMap<Integer, ScriptFieldDefinition> getFieldDefinitionData()
    {
        ScriptableHashMap<Integer, ScriptFieldDefinition> result =
            new ScriptableHashMap<Integer, ScriptFieldDefinition>();
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        
        // An Integer-based Map is being used here as we need to allow field definitions
        // for both properties and associations. It is possible for a property and an
        // association to coexist with the same name.
        int i = 0;
        for (FieldDefinition fd : fieldDefs)
        {
            result.put(i++, new ScriptFieldDefinition(fd));
        }
        return result;
    }

    public ScriptFormData getFormData()
    {
        return new ScriptFormData(form.getFormData());
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ScriptForm:").append(form.getItem());
        return builder.toString();
    }
}
