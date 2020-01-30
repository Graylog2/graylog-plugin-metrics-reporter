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

import com.google.common.primitives.Ints;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class PushGatewayPeriodicalTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private final MetricsPrometheusReporterConfiguration configuration = new MetricsPrometheusReporterConfiguration();
    private final CollectorRegistry collectorRegistry = new CollectorRegistry();

    @Mock
    private PushGateway pushGateway;
    private PushGatewayPeriodical periodical;

    @Before
    public void setUp() throws Exception {
        periodical = new PushGatewayPeriodical(configuration, collectorRegistry, pushGateway);
    }

    @Test
    public void doRunReportsMetrics() throws Exception {
        periodical.doRun();
        verify(pushGateway).push(collectorRegistry, configuration.getJobName(), configuration.getGroupingKey());
    }

    @Test
    public void doRunHandlesException() throws Exception {
        doThrow(new IOException("Test"))
                .when(pushGateway).push(collectorRegistry, configuration.getJobName(), configuration.getGroupingKey());
        periodical.doRun();
    }

    @Test
    public void runsForeverIsFalse() throws Exception {
        assertFalse(periodical.runsForever());
    }

    @Test
    public void stopOnGracefulShutdownIsTrue() throws Exception {
        assertTrue(periodical.stopOnGracefulShutdown());
    }

    @Test
    public void masterOnlyIsFalse() throws Exception {
        assertFalse(periodical.masterOnly());
    }

    @Test
    public void startOnThisNodeCorrespondsToConfiguration() throws Exception {
        assertEquals(configuration.isEnabled(), periodical.startOnThisNode());
    }

    @Test
    public void isDaemonIsTrue() throws Exception {
        assertTrue(periodical.isDaemon());
    }

    @Test
    public void getInitialDelaySecondsIsZero() throws Exception {
        assertEquals(0, periodical.getInitialDelaySeconds());
    }

    @Test
    public void getPeriodSecondsCorrespondesToConfiguration() throws Exception {
        final int reportIntervalInSeconds = Ints.saturatedCast(configuration.getReportInterval().toSeconds());
        assertEquals(reportIntervalInSeconds, periodical.getPeriodSeconds());
    }
}
