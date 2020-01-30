/**
 * This file is part of Graylog Metrics Prometheus Reporter Plugin.
 *
 * Graylog Metrics Prometheus Reporter Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog Metrics Prometheus Reporter Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog Metrics Prometheus Reporter Plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.plugins.metrics.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;
import org.graylog.plugins.metrics.prometheus.providers.CollectorRegistryProvider;
import org.graylog.plugins.metrics.prometheus.providers.DropwizardExportsProvider;
import org.graylog.plugins.metrics.prometheus.providers.DropwizardSampleBuilderProvider;
import org.graylog.plugins.metrics.prometheus.providers.PushGatewayProvider;
import org.graylog.plugins.metrics.prometheus.rest.MetricsResource;
import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;

import java.util.Collections;
import java.util.Set;

public class MetricsPrometheusReporterModule extends PluginModule {
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.singleton(new MetricsPrometheusReporterConfiguration());
    }

    @Override
    protected void configure() {
        addConfigBeans();

        bind(DropwizardSampleBuilder.class).toProvider(DropwizardSampleBuilderProvider.class);
        bind(DropwizardExports.class).toProvider(DropwizardExportsProvider.class);
        bind(CollectorRegistry.class).toProvider(CollectorRegistryProvider.class);
        bind(PushGateway.class).toProvider(PushGatewayProvider.class);

        addPeriodical(PushGatewayPeriodical.class);
        addRestResource(MetricsResource.class);
    }
}
