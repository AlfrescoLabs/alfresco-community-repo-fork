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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Test for ACS-1291 to make it simpler to profile.
 *
 * @author adavis
 */
@Category(PerformanceTests.class)
public class Acs1291PerformanceTest extends BaseSpringTest // AbstractContextAwareRepoEvent
{
    private static final String TEST_NAMESPACE  = "http://www.alfresco.org/test/acs1291";

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
    }

    private NodeRef createNode()
    {
        return retryingTransactionHelper.doInTransaction(() -> nodeService.createNode(rootNodeRef,
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
        // ACS 6.2.2.12             cold: 131507 warm: 145680, 139251, 137775, 132992 (avg 13ms 137441)
        // ACS 7.0.0 without events cold: 153874 warm: 155765, 152651, 135708, 135833 (avg 14ms 146766 6% slower, but some faster)
        // ACS 7.0.0                cold: 180066 warm: 184115, 195903, 196393, 180135 (avg 18ms 187322 36% slower)
        final int NODES = 10000;
        final long TOTAL_622_TIME = (131507+145680+139251+137775+132992)/5;
        final long TOTAL_700_NO_EVENTS_TIME = (153874+155765+152651+135708+135833)/5;
        final long TOTAL_700_TIME = (180066+184115+195903+196393+180135)/5;

        final int BATCH = 100;
        final long BATCH_622_TIME = TOTAL_622_TIME * BATCH / NODES;

        NodeRef nodeRef = createNode();

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

    // Try and find the throughput limit for the system, so we can compare 6.2.2.12 against 7.0.0
    // 7.0.0 requests should be slower as they need to wait for activeMQ, but the throughput should be similar.
    @Test
    public void rampUpThreads() throws InterruptedException
    {
        final long PERIOD_MS = 5000;
        final long SLOWER_PERIOD_COUNT = 24; // If all batches are slower for 2 minutes stop
        final int THREAD_EVERY_PERIODS = 5;

        final AtomicBoolean stop = new AtomicBoolean(false);
        final long start = System.currentTimeMillis();
        final AtomicLong endOfPeriod = new AtomicLong(start+PERIOD_MS);
        final AtomicInteger periods = new AtomicInteger(0);
        final AtomicInteger updates = new AtomicInteger(0);
        final AtomicInteger threadCount = new AtomicInteger(0);
        final AtomicLong maxUpdates = new AtomicLong(-1);
        final AtomicInteger periodsSinceMaxUpdate = new AtomicInteger(0);
        final AtomicInteger totalUpdates = new AtomicInteger(0);

        ExecutorService executorService = Executors.newCachedThreadPool();

        class UpdateRunnable implements Runnable
        {
            @Override
            public void run()
            {
                NodeRef nodeRef = createNode();
                final AtomicInteger i = new AtomicInteger(0);

                threadCount.incrementAndGet();
                while (!stop.get())
                {
                    retryingTransactionHelper.doInTransaction(() ->
                    {
                        String value = "test title " + i.incrementAndGet();
                        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, value);
                        totalUpdates.incrementAndGet();
                        return null;
                    });

                    // Increment the count, start a new period if required and stop if we are no longer getting better.
                    String message = null;
                    synchronized (this)
                    {
                        int updateCount = updates.getAndIncrement();
                        long now = System.currentTimeMillis();
                        long end = endOfPeriod.get();
                        if (now > end)
                        {
                            int period;
                            int sinceMax;
                            do
                            {
                                period = periods.incrementAndGet();
                                sinceMax = periodsSinceMaxUpdate.incrementAndGet();
                                end += PERIOD_MS;
                                endOfPeriod.set(end);
                                if (period % THREAD_EVERY_PERIODS == 0)
                                {
                                    executorService.submit(new UpdateRunnable());
                                }
                            } while (now > end);
                            updates.set(0);

                            message = period + ": Threads: "+threadCount.get()+" updates:"+updateCount+" "+
                                    (PERIOD_MS/updateCount) + "ms";

                            if (updateCount >= maxUpdates.get())
                            {
                                maxUpdates.set(updateCount);
                                periodsSinceMaxUpdate.set(0);
                                message += " ***";
                            }
                            else
                            {
                                message += " "+sinceMax;
                                if (sinceMax >= SLOWER_PERIOD_COUNT)
                                {
                                    stop.set(true);
                                    List<Runnable> runnables = executorService.shutdownNow();
                                }
                            }
                        }
                    }
                    if (message != null)
                    {
                        System.out.println(message);
                    }
                }
            }
        }

        executorService.submit(new UpdateRunnable());
        executorService.awaitTermination(60, MINUTES);
    }
}
