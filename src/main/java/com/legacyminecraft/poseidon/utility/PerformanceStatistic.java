package com.legacyminecraft.poseidon.utility;

public class PerformanceStatistic {
    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;
    private long totalTime = 0;
    private long count = 0;

    // Update the stats with a new execution time
    public void update(long duration) {
        if (duration < minTime) {
            minTime = duration;
        }
        if (duration > maxTime) {
            maxTime = duration;
        }
        totalTime += duration;
        count++;
    }

    // Retrieve the average execution time
    long getAverageTime() {
        return count > 0 ? totalTime / count : 0;
    }

    // Print the statistics for this listener
    public String printStats() {
        return String.format("Called: %d times, Min: %dms, Avg: %dms, Max: %dms", count, minTime, getAverageTime(), maxTime);
    }

    public long getEventCount() {
        return count;
    }

    public long getTotalExecutionTime() {
        return totalTime;
    }

    public long getAverageExecutionTime() {
        return getAverageTime();
    }

    public long getMinExecutionTime() {
        return minTime;
    }

    public long getMaxExecutionTime() {
        return maxTime;
    }
}
