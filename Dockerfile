FROM golang:1.9.2
RUN mkdir /var/grafana-ext
WORKDIR /var/
RUN mkdir ./grafana-ext/bin/
RUN mkdir ./grafana-ext/pkg/
RUN mkdir ./grafana-ext/src/
ADD ./grafana-ext ./grafana-ext
WORKDIR /app/grafana-ext/src/grafana/
RUN go run build.go setup
RUN go run build.go build