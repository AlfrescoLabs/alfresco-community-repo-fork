/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.PerformanceTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for ACS-1291 to make it simpler to profile.
 *
 * @author adavis
 */
@Category(PerformanceTests.class)
public class Acs1291PerformanceTest extends BaseSpringTest // AbstractContextAwareRepoEvent
{
    private static final String TEST_NAMESPACE  = "http://www.alfresco.org/test/acs1291";

    // ACS 6.2.2.12             cold: 131507 warm: 145680, 139251, 137775, 132992 (avg 13ms 137441)
    // ACS 7.0.0 without events cold: 153874 warm: 155765, 152651, 135708, 135833 (avg 14ms 146766 6% slower, but some faster)
    // ACS 7.0.0                cold: 180066 warm: 184115, 195903, 196393, 180135 (avg 18ms 187322 36% slower)

    private static final int NODES = 10000;
    private static final long TOTAL_622_TIME = (131507+145680+139251+137775+132992)/5;
    private static final long TOTAL_700_NO_EVENTS_TIME = (153874+155765+152651+135708+135833)/5;
    private static final long TOTAL_700_TIME = (180066+184115+195903+196393+180135)/5;

    private static final int BATCH = 100;
    private static final long BATCH_622_TIME = TOTAL_622_TIME * BATCH / NODES;

    @Autowired
    protected RetryingTransactionHelper retryingTransactionHelper;

    @Autowired
    protected NodeService nodeService;

    private NodeRef rootNodeRef;
    private NodeRef nodeRef;

    @Before
    public void setUp() throws Exception
    {
        // authenticate as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        rootNodeRef = retryingTransactionHelper.doInTransaction(() -> {
            // create a store and get the root node
            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, getClass().getName());
            if (!nodeService.exists(storeRef))
            {
                storeRef = nodeService.createStore(storeRef.getProtocol(),
                        storeRef.getIdentifier());
            }
            return nodeService.getRootNode(storeRef);
        });

        nodeRef = retryingTransactionHelper.doInTransaction(() -> nodeService.createNode(rootNodeRef,
                ContentModel.ASSOC_CHILDREN, QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef());
    }

    @After
    public void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void updateTitle()
    {
        long start = System.currentTimeMillis();
        long startOfBatch = start;
        final AtomicInteger i = new AtomicInteger(0);

        System.err.println("ACS 6.2.2           "+NODES+": "+ (TOTAL_622_TIME/NODES)          + "ms " +
                (((TOTAL_622_TIME          *100)/TOTAL_622_TIME-100)*-1)+"% " +
                TOTAL_622_TIME + "ms (-ve % is slower)");
        System.err.println("ACS 7.0.0 no events "+NODES+": "+ (TOTAL_700_NO_EVENTS_TIME/NODES)+ "ms " +
                (((TOTAL_700_NO_EVENTS_TIME*100)/TOTAL_622_TIME-100)*-1)+"% " +
                TOTAL_700_NO_EVENTS_TIME + "ms");
        System.err.println("ACS 7.0.0           "+NODES+": "+ (TOTAL_700_TIME/NODES)          + "ms " +
                (((TOTAL_700_TIME          *100)/TOTAL_622_TIME-100)*-1)+"% " +
                TOTAL_700_TIME + "ms\n");
        while (i.getAndIncrement() < NODES)
        {
            retryingTransactionHelper.doInTransaction(() -> {
                String value = "test title " + i.get();
                nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, value);
                return null;
            });
            int l = i.get();
            if (l % BATCH == 0)
            {
                long batch = l / BATCH;
                long now = System.currentTimeMillis();
                long ms = now - start;
                long batchMs = now - startOfBatch;
                System.err.println(l + ": " +
                        (batchMs/BATCH) + "ms "+
                        (ms/l)+ "ms " +
                        (((batchMs*100)/BATCH_622_TIME-100)*-1)+"% " +
                        (((ms*100)/(BATCH_622_TIME*batch)-100)*-1)+"%  " +
                        ms + "ms");
                startOfBatch = now;
            }
        }
        long ms = System.currentTimeMillis() - start;
        System.err.println("Performance: " + (((ms*100)/ TOTAL_622_TIME -100)*-1)+"%");
    }
}
