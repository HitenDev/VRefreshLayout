package com.leelay.refresh.vertical.config;

/**
 * Created by leelay on 2016/12/6.
 */

public class Config {

    private static Config config = new Config();
    public float dragRate;
    public float ratioOfHeaderHeightToReach;
    public int completeStickDuration;
    public int autoRefreshDuration;
    public int toStartDuration;
    public int toRetainDuration;

    private Config() {
        reset();
    }

    public static Config getInstance() {
        return config;
    }

    public void reset() {
        dragRate = 0.5f;
        ratioOfHeaderHeightToReach = 1.6f;
        completeStickDuration = 400;
        autoRefreshDuration = 800;
        toStartDuration = 200;
        toRetainDuration = 200;
    }
}
