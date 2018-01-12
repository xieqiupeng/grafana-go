package api

import (
	"fmt"
	"net/http"

	"grafana/pkg/components/renderer"
	"grafana/pkg/middleware"
	"grafana/pkg/util"
)

func RenderToPng(c *middleware.Context) {
	queryReader := util.NewUrlQueryReader(c.Req.URL)
	queryParams := fmt.Sprintf("?%s", c.Req.URL.RawQuery)

	renderOpts := &renderer.RenderOpts{
		Path:     c.Params("*") + queryParams,
		Width:    queryReader.Get("width", "800"),
		Height:   queryReader.Get("height", "400"),
		OrgId:    c.OrgId,
		Timeout:  queryReader.Get("timeout", "60"),
		Timezone: queryReader.Get("tz", ""),
	}

	pngPath, err := renderer.RenderToPng(renderOpts)

	if err != nil {
		if err == renderer.ErrTimeout {
			c.Handle(500, err.Error(), err)
		}

		c.Handle(500, "Rendering failed.", err)
		return
	}

	c.Resp.Header().Set("Content-Type", "image/png")
	http.ServeFile(c.Resp, c.Req.Request, pngPath)
}
