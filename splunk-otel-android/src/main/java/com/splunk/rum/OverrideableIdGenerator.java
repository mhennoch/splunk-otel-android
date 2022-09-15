package com.splunk.rum;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.IdGenerator;

public class OverrideableIdGenerator implements IdGenerator {
    private final ThreadLocal<ScopedOverride> threadLocalOverride = new ThreadLocal<>();
    private final IdGenerator delegate;

    public OverrideableIdGenerator(IdGenerator delegate) {
        this.delegate = delegate;
    }

    public Scope override(String traceIdHex, String spanIdHex) {
        ScopedOverride override = new ScopedOverride(traceIdHex, spanIdHex);
        threadLocalOverride.set(override);
        return override;
    }

    @Override
    public String generateSpanId() {
        ScopedOverride ids = threadLocalOverride.get();
        if (ids != null) {
            return ids.spanIdHex;
        } else {
            return delegate.generateSpanId();
        }
    }

    @Override
    public String generateTraceId() {
        ScopedOverride ids = threadLocalOverride.get();
        if (ids != null) {
            return ids.traceIdHex;
        } else {
            return delegate.generateTraceId();
        }
    }

    private class ScopedOverride implements Scope {
        private final String traceIdHex;
        private final String spanIdHex;

        private ScopedOverride(String traceIdHex, String spanIdHex) {
            this.traceIdHex = traceIdHex;
            this.spanIdHex = spanIdHex;
        }

        @Override
        public void close() {
            threadLocalOverride.remove();
        }
    }
}
