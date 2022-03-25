package com.ubcohci.fingerdetection;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A tracker for inference information (i.e. Latency)
 * Latency is calculated in milliseconds.
 */
public class InferenceTracker {

    // Keep a queue of latency
    private final Queue<Long> queue;

    // Max size of queue
    private final static int queueSize = 10;

    // Keep a marker for start time
    private long startTime;

    public InferenceTracker() {
        this.queue = new LinkedList<>();
    }

    public void addNewLatency(long latency) {
        if (queue.size() == queueSize) {
            queue.remove(); // Remove the first element
        }
        queue.add(latency);
    }

    public long getLatency() {
        return getQueueSum(this.queue) / Math.max(1, this.queue.size());
    }

    public void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public void setStopTime() {
        // Keep a marker for stop time
        long stopTime = System.currentTimeMillis();
        addNewLatency(stopTime - this.startTime);
    }

    private static long getQueueSum(Queue<Long> queue) {
        long sum = 0;
        for (Long integer: queue) {
            sum += integer;
        }
        return sum;
    }
}
