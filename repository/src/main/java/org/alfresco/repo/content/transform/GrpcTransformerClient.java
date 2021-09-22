/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.transformer.grpc.MultipartFile;
import org.alfresco.transformer.grpc.TransformReply;
import org.alfresco.transformer.grpc.TransformRequest;
import org.alfresco.transformer.grpc.TransformServiceGrpc;
import org.alfresco.util.Pair;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;

import java.io.IOException;

public class GrpcTransformerClient
{
    private final String name;
    private final String baseUrl;

    // The length of time to wait after a connection problem before checking availability again.
    private long startupRetryPeriod = 15000;

    // When to check availability.
    private long checkAvailabilityAfter = 0L;

    // The initial value indicates we have not had a success yet.
    // Only changed once on success. This is stored so it can always be returned.
    private Pair<Boolean, String> checkResult = new Pair<>(null, null);

    public GrpcTransformerClient(String name, String baseUrl)
    {
        this.name = name;
        this.baseUrl = baseUrl == null || baseUrl.trim().isEmpty() ? null : baseUrl.trim();
    }

    public void setStartupRetryPeriodSeconds(int startupRetryPeriodSeconds)
    {
        startupRetryPeriod = startupRetryPeriodSeconds*1000;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void request(ContentReader reader, ContentWriter writer, String sourceMimetype, String sourceExtension,
                        String targetExtension, long timeoutMs, Log logger, String... args)
    {
        if (args.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should be a value for each request property");
        }

        logger.debug("From GRPC" +  name+' '+sourceExtension+' '+targetExtension+' '+baseUrl+' '+args);

        try
        {

            ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9096").usePlaintext().build();

            TransformServiceGrpc.TransformServiceBlockingStub transformServiceBlockingStub = TransformServiceGrpc.newBlockingStub(channel);
            TransformRequest transformRequest = TransformRequest.newBuilder()
                    .setFile(MultipartFile.newBuilder()
                            .setFile(ByteString.copyFrom(reader.getContentInputStream().readAllBytes()))
                            .setOrginalFileName("tmp."+sourceExtension)
                            .build())
                    .setOriginalFileName("tmp."+sourceExtension)
                    .setSourceMediaType(sourceMimetype)
                    .setSourceExtension(sourceExtension)
                    .setTargetExtension(targetExtension)
                    .build();

            TransformReply transformReply = transformServiceBlockingStub.transform(transformRequest);
            writer.putContent(transformReply.getFile().newInput());
        }
        catch (AlfrescoRuntimeException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage());
            }
            throw e;
        }
        catch (IOException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage());
            }
            throw new AlfrescoRuntimeException("failed to parse file", e);
        }
    }

    /**
     *  Indicates if a remote transform:
     *  a) ready probe has ever indicated success {@code new Pair<>(true, <version string>)},
     *  b) a ready probe has just failed {@code new Pair<>(false, <error string>)}, or
     *  c) we are not performing a ready check as we have just done one {@code new Pair<>(null, null)}.
     */
    public Pair<Boolean, String> check(Log logger)
    {
        if (!isTimeToCheckAvailability())
        {
            logger.debug(name+' '+" too early to check availability");
            Pair<Boolean, String> result = getCheckResult();
            return result;
        }
        connectionSuccess();

       return new Pair<>(true, "1.0.0");
    }

    /**
     * Helper method that returns the same result type as {@link #check(Log)} given a local checkCommand.
     */
    public static Pair<Boolean,String> check(RuntimeExec checkCommand)
    {
        ExecutionResult result = checkCommand.execute();
        Boolean success = result.getSuccess();
        String output = success ? result.getStdOut().trim() : result.toString();
        return new Pair<>(success, output);
    }

    synchronized void connectionFailed()
    {
        checkAvailabilityAfter = System.currentTimeMillis() + startupRetryPeriod;
    }

    synchronized void connectionSuccess()
    {
        checkAvailabilityAfter = Long.MAX_VALUE;
    }

    private synchronized boolean isTimeToCheckAvailability()
    {
        return System.currentTimeMillis() > checkAvailabilityAfter;
    }

    public synchronized boolean isAvailable()
    {
        return checkAvailabilityAfter == Long.MAX_VALUE;
    }

    private synchronized Pair<Boolean, String> getCheckResult()
    {
        return checkResult;
    }
}
