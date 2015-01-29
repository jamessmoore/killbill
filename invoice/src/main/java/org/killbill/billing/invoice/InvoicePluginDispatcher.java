/*
 * Copyright 2014-2015 Groupon, Inc
 * Copyright 2014-2015 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
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

package org.killbill.billing.invoice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoiceItemType;
import org.killbill.billing.invoice.plugin.api.InvoicePluginApi;
import org.killbill.billing.osgi.api.OSGIServiceRegistration;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.util.callcontext.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class InvoicePluginDispatcher {

    private static final Logger log = LoggerFactory.getLogger(InvoicePluginDispatcher.class);

    private final OSGIServiceRegistration<InvoicePluginApi> pluginRegistry;

    @Inject
    public InvoicePluginDispatcher(final OSGIServiceRegistration<InvoicePluginApi> pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

    public List<InvoiceItem> getAdditionalInvoiceItems(final Invoice originalInvoice, final CallContext callContext) {
        // TODO immutable invoice
        final Invoice immutableInvoice = originalInvoice;

        final List<InvoiceItem> additionalInvoiceItems = new LinkedList<InvoiceItem>();
        final List<InvoicePluginApi> invoicePlugins = getInvoicePlugins();
        for (final InvoicePluginApi invoicePlugin : invoicePlugins) {
            final List<InvoiceItem> items = invoicePlugin.getAdditionalInvoiceItems(immutableInvoice, ImmutableList.<PluginProperty>of(), callContext);
            if (items != null) {
                for (final InvoiceItem item : items) {
                    if (item.getInvoiceItemType() != InvoiceItemType.FIXED &&
                        item.getInvoiceItemType() != InvoiceItemType.RECURRING &&
                        item.getInvoiceItemType() != InvoiceItemType.REPAIR_ADJ &&
                        item.getInvoiceItemType() != InvoiceItemType.CBA_ADJ &&
                        item.getInvoiceItemType() != InvoiceItemType.CREDIT_ADJ &&
                        item.getInvoiceItemType() != InvoiceItemType.REFUND_ADJ &&
                        item.getInvoiceItemType() != InvoiceItemType.USAGE) {
                        additionalInvoiceItems.add(item);
                    } else {
                        log.warn("Ignoring invoice item of type {} from InvoicePlugin {}: {}", item.getInvoiceItemType(), invoicePlugin, item);
                    }
                }
            }
        }
        return additionalInvoiceItems;
    }

    private List<InvoicePluginApi> getInvoicePlugins() {
        final List<InvoicePluginApi> invoicePlugins = new ArrayList<InvoicePluginApi>();
        for (final String name : pluginRegistry.getAllServices()) {
            invoicePlugins.add(pluginRegistry.getServiceForName(name));
        }
        return invoicePlugins;
    }
}