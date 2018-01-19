FROM golang:1.9.2
WORKDIR grafana-ext/src/grafana/
RUN go run build.go setup
RUN go run build.go build