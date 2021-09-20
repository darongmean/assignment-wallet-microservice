package com.darongmean.common;

public final class TraceId {
    public static String format(String traceId) {
        if (traceId == null) {
            return null;
        }
        return traceId.substring(0, Math.min(traceId.length(), 255));
    }
}
