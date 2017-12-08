package defaults

import (
	// register standard active monitors
	_ "heartbeat/monitors/active/http"
	_ "heartbeat/monitors/active/icmp"
	_ "heartbeat/monitors/active/tcp"
)
