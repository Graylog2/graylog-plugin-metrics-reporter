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
package org.graylog.plugins.metrics.prometheus.providers;

import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;

import org.graylog.plugins.metrics.prometheus.DropwizardSampleBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Objects.requireNonNull;


public class DropwizardSampleBuilderProvider implements Provider<DropwizardSampleBuilder> {
    private final StreamService streamService;
    private final StreamRuleService streamRuleService;

    @Inject
    public DropwizardSampleBuilderProvider(StreamService streamService, StreamRuleService streamRuleService) {
        this.streamRuleService = requireNonNull(streamRuleService);
        this.streamService =  requireNonNull(streamService);
    }

    @Override
    public DropwizardSampleBuilder get() {
        return new DropwizardSampleBuilder(streamService, streamRuleService);
    }
}
