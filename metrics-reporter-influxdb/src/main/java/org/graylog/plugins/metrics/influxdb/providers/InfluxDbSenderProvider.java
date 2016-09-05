/**
 * This file is part of Graylog Metrics InfluxDB Reporter Plugin.
 *
 * Graylog Metrics InfluxDB Reporter Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog Metrics InfluxDB Reporter Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog Metrics InfluxDB Reporter Plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.plugins.metrics.influxdb.providers;

import com.google.common.base.Throwables;
import com.google.common.primitives.Ints;
import com.izettle.metrics.influxdb.InfluxDbHttpSender;
import com.izettle.metrics.influxdb.InfluxDbSender;
import com.izettle.metrics.influxdb.InfluxDbTcpSender;
import com.izettle.metrics.influxdb.InfluxDbUdpSender;
import org.graylog.plugins.metrics.influxdb.MetricsInfluxDbReporterConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URI;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class InfluxDbSenderProvider implements Provider<InfluxDbSender> {
    private final MetricsInfluxDbReporterConfiguration configuration;

    @Inject
    public InfluxDbSenderProvider(MetricsInfluxDbReporterConfiguration configuration) {
        this.configuration = requireNonNull(configuration);
    }

    @Override
    public InfluxDbSender get() {
        try {
            final URI uri = configuration.getUri().parseServerAuthority();
            final String protocol = uri.getScheme().toLowerCase(Locale.ENGLISH);

            switch (protocol) {
                case "http":
                case "https":
                    return new InfluxDbHttpSender(
                            protocol,
                            uri.getHost(),
                            uri.getPort(),
                            uri.getPath().replaceFirst("^/", ""),
                            uri.getUserInfo(),
                            configuration.getTimePrecision(),
                            Ints.saturatedCast(configuration.getConnectTimeout().toMilliseconds()),
                            Ints.saturatedCast(configuration.getReadTimeout().toMilliseconds()),
                            "");
                case "tcp":
                    return new InfluxDbTcpSender(
                            uri.getHost(),
                            uri.getPort(),
                            Ints.saturatedCast(configuration.getSocketTimeout().toMilliseconds()),
                            uri.getPath(),
                            configuration.getTimePrecision(),
                            "");
                case "udp":
                    return new InfluxDbUdpSender(
                            uri.getHost(),
                            uri.getPort(),
                            Ints.saturatedCast(configuration.getSocketTimeout().toMilliseconds()),
                            uri.getPath(),
                            configuration.getTimePrecision(),
                            "");
                default:
                    throw new IllegalArgumentException("Unsupported protocol \"" + protocol + "\"");
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
