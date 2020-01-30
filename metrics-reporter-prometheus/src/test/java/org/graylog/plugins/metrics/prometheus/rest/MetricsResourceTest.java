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
package org.graylog.plugins.metrics.prometheus.rest;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.graylog.plugins.metrics.prometheus.MetricsPrometheusReporterModule;
import org.graylog2.shared.bindings.GuiceInjectorHolder;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MetricsResourceTest extends JerseyTest {
    public MetricsResourceTest() {
        final Module metricsModule = binder -> {
            final MetricRegistry registry = new MetricRegistry();
            final StreamRuleService srs = mock(StreamRuleService.class);
            final StreamService ss = mock(StreamService.class);
            final Counter counter = registry.counter("test.counter");
            counter.inc(42L);

            binder.bind(StreamRuleService.class).toInstance(srs);
            binder.bind(StreamService.class).toInstance(ss);
            binder.bind(MetricRegistry.class).toInstance(registry);
        };
        GuiceInjectorHolder.createInjector(ImmutableList.of(new MetricsPrometheusReporterModule(), metricsModule));
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MetricsResource.class);
    }

    @Test
    public void prometheusMetricStreamsMetrics() throws Exception {
        final Response response = target("/metrics").request().get();

        assertEquals(200, response.getStatus());
        assertEquals("text/plain;charset=utf-8;version=0.0.4", response.getHeaderString("Content-Type"));

        final String body = response.readEntity(String.class);
        assertTrue(body.endsWith("test_counter 42.0\n"));
    }
}
