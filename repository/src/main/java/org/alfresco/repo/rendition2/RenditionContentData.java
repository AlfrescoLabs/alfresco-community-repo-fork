/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
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
package org.alfresco.repo.rendition2;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.StringTokenizer;

public class RenditionContentData implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final String RENDITION_NAME_IDENTIFIER = "renditionName=";
    private static final String LAST_MODIFIED_IDENTIFIER = "lastModified=";
    private static final String TRANSFORM_HASH_CODE_IDENTIFIER = "transformHashCode=";
    private String renditionName;
    private ContentData contentData;
    private Long lastModified; // todo - would it be better to have something from java.time package?
    private Integer transformContentHashCode;

    public RenditionContentData(String renditionName,
                                Long lastModified,
                                Integer transformContentHashCode,
                                ContentData contentData)
    {
        this.renditionName = renditionName;
        this.lastModified = lastModified;
        this.transformContentHashCode = transformContentHashCode;
        this.contentData = contentData;
    }

    public static RenditionContentData getRenditionContentData(ContentData contentData, String renditionName)
    {
        RenditionContentData renditionContentData = new RenditionContentData();
        renditionContentData.renditionName= renditionName;
        renditionContentData.contentData= contentData;
        return renditionContentData;
    }

    private RenditionContentData()
    {

    }

    public RenditionContentData(String renditionContentStr)
    {
        if (renditionContentStr == null || renditionContentStr.isBlank())
        {
            // todo - throw appropriate exception
        }

        StringTokenizer tokenizer = new StringTokenizer(renditionContentStr, "|");
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if (token.startsWith(RENDITION_NAME_IDENTIFIER))
            {
                renditionName = token.substring(RENDITION_NAME_IDENTIFIER.length());
                // todo - would it be better to not allow missing values?
                if (renditionName.isBlank())
                {
                    renditionName = null;
                }
            }
            if (token.startsWith(TRANSFORM_HASH_CODE_IDENTIFIER))
            {
                String transformContentHashCodeStr = token.substring(TRANSFORM_HASH_CODE_IDENTIFIER.length());
                try
                {
                    transformContentHashCode = Integer.valueOf(transformContentHashCodeStr);
                }
                catch (Exception e)
                {
                    // todo - would it be better to not allow missing values?
                    // failed to parse hash code
                }
            }
            if (token.startsWith(LAST_MODIFIED_IDENTIFIER))
            {
                String lastModifiedStr = token.substring(LAST_MODIFIED_IDENTIFIER.length());
                try
                {
                    lastModified = Long.valueOf(lastModifiedStr);
                }
                catch (Exception e)
                {
                    // todo - would it be better to not allow missing values?
                    // failed to parse last modified
                }
            }
        }
        contentData = ContentData.createContentProperty(renditionContentStr);
    }

    public String getRenditionName()
    {
        return renditionName;
    }

    public void setRenditionName(String renditionName)
    {
        this.renditionName = renditionName;
    }

    public ContentData getContentData()
    {
        return contentData;
    }

    public void setContentData(ContentData contentData)
    {
        this.contentData = contentData;
    }

    public Long getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(Long lastModified)
    {
        this.lastModified = lastModified;
    }

    public Integer getTransformContentHashCode()
    {
        return transformContentHashCode;
    }

    public void setTransformContentHashCode(Integer transformContentHashCode)
    {
        this.transformContentHashCode = transformContentHashCode;
    }

    public String getInfoUrl()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(RENDITION_NAME_IDENTIFIER).append(renditionName)
                .append("|"+ LAST_MODIFIED_IDENTIFIER).append(lastModified)
                .append("|"+ TRANSFORM_HASH_CODE_IDENTIFIER).append(transformContentHashCode)
                .append("|"+ contentData.getInfoUrl());
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return getInfoUrl();
    }

    // todo - hash, equals
}
