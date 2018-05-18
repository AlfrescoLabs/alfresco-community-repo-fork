/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.v0.service;


import java.util.HashMap;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for different disposition schedule actions
 *
 * @author jcule
 * @since 2.7.0.1
 */
@Service
public class DispositionScheduleService extends BaseAPI
{
    @Autowired
    private RecordCategoriesAPI recordCategoriesAPI;

    @Autowired
    private DataUser dataUser;

    /**
     * Helper method for adding a cut off after period step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param period
     * @return
     */
    public void addCutOffAfterPeriodStep(String categoryName, String period)
    {
        HashMap<RETENTION_SCHEDULE, String> cutOffStep = new HashMap<>();
        cutOffStep.put(RETENTION_SCHEDULE.NAME, "cutoff");
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        cutOffStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Cut off after a period step");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, cutOffStep);
    }

    /**
     * Helper method for adding a cut off after an event occurs step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param events
     */
    public void addCutOffAfterEventStep(String categoryName, String events)
    {
        HashMap<RETENTION_SCHEDULE, String> cutOffStep = new HashMap<>();
        cutOffStep.put(RETENTION_SCHEDULE.NAME, "cutoff");
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_EVENTS, events);
        cutOffStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Cut off after event step");

        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, cutOffStep);
    }

    /**
     * Helper method for accession step properties
     *
     * @param timeOrEvent
     * @param events
     * @param period
     * @param periodProperty
     * @param combineConditions
     * @return
     */
    public void addAccessionStep(String categoryName, Boolean timeOrEvent, String events, String period, String
            periodProperty, Boolean combineConditions)
    {
        HashMap<RETENTION_SCHEDULE, String> accessionStep = new HashMap<>();
        accessionStep.put(RETENTION_SCHEDULE.NAME, "accession");
        accessionStep.put(RETENTION_SCHEDULE.COMBINE_DISPOSITION_STEP_CONDITIONS, Boolean.toString(combineConditions));
        accessionStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        accessionStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD_PROPERTY, periodProperty);
        if (!timeOrEvent)
        {
            accessionStep.put(RETENTION_SCHEDULE.RETENTION_ELIGIBLE_FIRST_EVENT, Boolean.toString(timeOrEvent));
        }
        accessionStep.put(RETENTION_SCHEDULE.RETENTION_EVENTS, events);
        accessionStep.put(RETENTION_SCHEDULE.DESCRIPTION,
                    "Accession step with time and event conditions.");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, accessionStep);
    }

    /**
     * Helper method to create retention schedule with general fields for the given category as admin
     * and apply it to the records
     *
     * @param categoryName
     * @param appliedToRecords
     */
    public void createCategoryRetentionSchedule(String categoryName, Boolean appliedToRecords)
    {
        recordCategoriesAPI.createRetentionSchedule(dataUser.getAdminUser().getUsername(),
                    dataUser.getAdminUser().getPassword(), categoryName);
        String retentionScheduleNodeRef = recordCategoriesAPI.getDispositionScheduleNodeRef(
                    dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), categoryName);

        HashMap<RETENTION_SCHEDULE, String> retentionScheduleGeneralFields = new HashMap<>();
        retentionScheduleGeneralFields.put(RETENTION_SCHEDULE.RETENTION_AUTHORITY, "Authority");
        retentionScheduleGeneralFields.put(RETENTION_SCHEDULE.RETENTION_INSTRUCTIONS, "Instructions");
        recordCategoriesAPI.setRetentionScheduleGeneralFields(dataUser.getAdminUser().getUsername(),
                    dataUser.getAdminUser().getPassword(), retentionScheduleNodeRef, retentionScheduleGeneralFields,
                    appliedToRecords);

    }
}
