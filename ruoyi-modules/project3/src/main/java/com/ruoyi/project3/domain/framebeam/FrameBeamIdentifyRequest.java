package com.ruoyi.project3.domain.framebeam;

public class FrameBeamIdentifyRequest
{
    private FrameBeamObjectInfo objectInfo;
    private FrameBeamIdentifyParams params;
    private String requestId;

    public FrameBeamObjectInfo getObjectInfo()
    {
        return objectInfo;
    }

    public void setObjectInfo(FrameBeamObjectInfo objectInfo)
    {
        this.objectInfo = objectInfo;
    }

    public FrameBeamIdentifyParams getParams()
    {
        return params;
    }

    public void setParams(FrameBeamIdentifyParams params)
    {
        this.params = params;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }
}
