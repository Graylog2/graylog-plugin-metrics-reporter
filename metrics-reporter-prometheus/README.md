# metrics-reporter-prometheus

This plugin exposes metrics to be scrapeable by prometheus.

It primarily converts the internally used Dropbox metrics one to one with a few
notable exceptions:

For any metric that has an ID embedded in its metric name, the id is removed
from the metric name and added as label `id` to prevent unnecessary
proliferation of metrics and make them easier to compare and/or aggregate.

For streams and stream rules additional information is provided as labels:

* `stream-title`: title of stream (stream and stream rule)
* `rule-type`: Type of stream rule, e.g. `regexp`
* `stream-id`: related stream id for stream rule
* `index-set-id`: ID of related index set for stream and stream rule

## Pulling metrics

The Prometheus metrics endpoint is available at `http://graylog.example.com:9000/api/plugins/org.graylog.plugins.metrics.prometheus/metrics`.

The corresponding configuration in `prometheus.yml` would look like the following code snippet:

```yaml
# See https://prometheus.io/docs/operating/configuration/#<scrape_config> for details
scrape_configs:
  - job_name: 'graylog'
    scrape_interval: 15s
    metrics_path: '/api/plugins/org.graylog.plugins.metrics.prometheus/metrics'
    basic_auth:
      username: graylog_username
      password: graylog_password
    # Optional TLS configuration; see https://prometheus.io/docs/operating/configuration/#<tls_config>
    #tls_config:
    static_configs:
      - targets: ['graylog1.example.com:9000', 'graylog2.example.com:9000']
```

## Configuration settings

| Setting                              | Default          | Description                                                                                       |
| ------------------------------------ | ---------------- | ------------------------------------------------------------------------------------------------- |
| `metrics_prometheus_enabled`         | `false`          | Whether to push the metrics to a [Prometheus Pushgateway].                                        |
| `metrics_prometheus_report_interval` | `15s`            | How often to report metrics to the [Prometheus Pushgateway].                                      |
| `metrics_prometheus_address`         | `127.0.0.1:9091` | The network address of the [Prometheus Pushgateway].                                              |
| `metrics_prometheus_job_name`        | `graylog`        | The job name used with the [Prometheus Pushgateway].                                              |
| `metrics_prometheus_grouping_key`    | [empty]          | The grouping key used with the [Prometheus Pushgateway] in the format `key1:value1, key2:value2`. |

[Prometheus Pushgateway]: https://github.com/prometheus/pushgateway
