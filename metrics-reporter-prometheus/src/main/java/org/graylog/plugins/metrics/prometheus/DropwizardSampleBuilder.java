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

import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.StreamRule;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;
import static io.prometheus.client.Collector.sanitizeMetricName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DropwizardSampleBuilder extends DefaultSampleBuilder {

    private static final String ID_METRIC_PATTERN = "(.*?)\\.([0-9a-f-]{8,})\\.(?:([0-9a-f-]{8,})\\.(\\d+)\\.)?+(.*?)";
    private static final String NESTED_STREAM_RULE = "(.*?_Stream_StreamRule_)([0-9a-f-]{8,})_(.+)";
    private final StreamService streamService;
    private final StreamRuleService streamRuleService;
    private final Pattern idPattern;
    private final Pattern nestedRulePattern;

    @Inject
    public DropwizardSampleBuilder(StreamService streamService,StreamRuleService streamRuleService) {
        this.streamService = requireNonNull(streamService);
        this.streamRuleService = requireNonNull(streamRuleService);
        this.idPattern =         Pattern.compile(ID_METRIC_PATTERN);
        this.nestedRulePattern = Pattern.compile(NESTED_STREAM_RULE);
    }

    @Override
    public Sample createSample(
        String dropwizardName,
        String nameSuffix,
        List<String> additionalLabelNames,
        List<String> additionalLabelValues,
        double value
    ) {

        final List<String> labelNames = additionalLabelNames == null ? new ArrayList<>()  : new ArrayList<>(additionalLabelNames);
        final List<String> labelValues = additionalLabelValues == null ? new ArrayList<>() : new ArrayList<>(additionalLabelValues);

        Matcher m = idPattern.matcher(dropwizardName);

        if (!m.matches()) {
            return new Collector.MetricFamilySamples.Sample(
                sanitizeMetricName(dropwizardName),
                new ArrayList<String>(labelNames),
                new ArrayList<String>(labelValues),
                value
            );
        }

        MatchResult result = m.toMatchResult();
        String metricName = sanitizeMetricName(String.join("_", Arrays.asList(
            result.group(1),
            result.group(5))
        ));

        String id = result.group(2);
        labelNames.add("id");
        labelValues.add(id);

        if (dropwizardName.contains(".ast.Rule.")) {
            if (result.group(3) != null) {
                labelNames.add("pipeline_id");
                labelValues.add(result.group(3));
            }

            if (result.group(4) != null) {
                labelNames.add("stage");
                labelValues.add(result.group(4));
            }
        } else if (dropwizardName.contains(".Stream.")) {
            try {
                Stream s = streamService.load(id);
                labelNames.add("stream_title");
                labelValues.add(s.getTitle());
                labelNames.add("index_set_id");
                labelValues.add(s.getIndexSetId());
            } catch (NotFoundException e) {
                // we'll have to live with less information I guess
            }

            Matcher nrm = nestedRulePattern.matcher(metricName);
            if (nrm.matches()) {
                metricName = nrm.group(1)+nrm.group(3);
                String streamRuleId = nrm.group(2);
                labelNames.add("stream_rule_id");
                labelValues.add(streamRuleId);

                try {
                    StreamRule rule = streamRuleService.load(streamRuleId);
                    String ruleType =  rule.getType().toString();
                    labelNames.add("rule_type");
                    labelValues.add(ruleType);
                } catch (NotFoundException nfe) {
                    labelNames.add("rule_type");
                    labelValues.add("unknown");
                }

            }
        } else if (dropwizardName.contains(".StreamRule.")) {
            try {
                StreamRule rule = streamRuleService.load(id);
                String ruleType =  rule.getType().toString();
                labelNames.add("rule_type");
                labelValues.add(ruleType);
                String streamId = rule.getStreamId();
                labelNames.add("stream_id");
                labelValues.add(streamId);

                try {
                    Stream stream = streamService.load(streamId);
                    labelNames.add("stream_title");
                    labelValues.add(stream.getTitle());
                    labelNames.add("index_set_id");
                    labelValues.add(stream.getIndexSetId());
                } catch (NotFoundException nfe) {
                    // we'll have to live with less information I guess
                }

            } catch (NotFoundException nfe) {
                labelNames.add("rule_type");
                labelValues.add("unknown");
            }

        }

        return new Collector.MetricFamilySamples.Sample(
                metricName,
                new ArrayList<String>(labelNames),
                new ArrayList<String>(labelValues),
                value
        );
    }
}
