package org.openl.vm.trace;

/**
 * This trace stack after reset removes only child trace objects (trace objects
 * added to decorated tracer will not be removed)
 * 
 * @author NSamatov
 * 
 */
public class ChildTraceStack implements TraceStack {
    private final TraceStack tracer;
    private final int initialSize;
    private int pushed = 0;

    public ChildTraceStack(TraceStack tracer) {
        this.tracer = tracer;
        initialSize = tracer.size();
    }

    @Override
    public void push(ITracerObject tracerObject) {
        tracer.push(tracerObject);
        pushed++;
    }

    @Override
    public void pop() {
        tracer.pop();
        pushed--;
    }

    @Override
    public void reset() {
        while (pushed > 0 && tracer.size() - initialSize > 0) {
            pop();
        }
    }

    @Override
    public int size() {
        return tracer.size();
    }

}