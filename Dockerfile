FROM meike/golang:alpine
WORKDIR grafana-ext/src/grafana/
RUN Makefile