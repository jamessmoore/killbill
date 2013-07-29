/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.subscription.api;

import java.util.ArrayList;
import java.util.List;

import com.ning.billing.catalog.api.CatalogService;
import com.ning.billing.subscription.api.user.SubscriptionBuilder;
import com.ning.billing.subscription.api.user.SubscriptionData;
import com.ning.billing.subscription.engine.dao.SubscriptionDao;
import com.ning.billing.subscription.events.SubscriptionEvent;
import com.ning.billing.subscription.api.user.Subscription;
import com.ning.billing.clock.Clock;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class SubscriptionApiBase {

    protected final SubscriptionDao dao;

    protected final SubscriptionApiService apiService;
    protected final Clock clock;
    protected final CatalogService catalogService;

    public SubscriptionApiBase(final SubscriptionDao dao, final SubscriptionApiService apiService, final Clock clock, final CatalogService catalogService) {
        this.dao = dao;
        this.apiService = apiService;
        this.clock = clock;
        this.catalogService = catalogService;
    }

    protected List<Subscription> createSubscriptionsForApiUse(final List<Subscription> internalSubscriptions) {
        return new ArrayList<Subscription>(Collections2.transform(internalSubscriptions, new Function<Subscription, Subscription>() {
            @Override
            public Subscription apply(final Subscription subscription) {
                return createSubscriptionForApiUse((SubscriptionData) subscription);
            }
        }));
    }

    protected SubscriptionData createSubscriptionForApiUse(final Subscription internalSubscription) {
        return new SubscriptionData((SubscriptionData) internalSubscription, apiService, clock);
    }

    protected SubscriptionData createSubscriptionForApiUse(SubscriptionBuilder builder, List<SubscriptionEvent> events) {
        final SubscriptionData subscription = new SubscriptionData(builder, apiService, clock);
        if (events.size() > 0) {
            subscription.rebuildTransitions(events, catalogService.getFullCatalog());
        }
        return subscription;
    }
}