package com.praetoriandroid.cameraremote.rpc

class ZoomRequest : BaseRequest<Void?, ActZoomResponse>(
        ActZoomResponse::class.java,
        RpcMethod.actZoom
)

