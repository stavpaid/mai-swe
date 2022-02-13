/*
 * Copyright 2021 Laszlo Balazs-Csiki and Contributors
 *
 * This file is part of Pixelitor. Pixelitor is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License, version 3 as published by the Free
 * Software Foundation.
 *
 * Pixelitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pixelitor. If not, see <http://www.gnu.org/licenses/>.
 */

package pixelitor.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A progress tracker that is used only for development
 * to check that the work units were correctly estimated.
 *
 * It can be a decorator for another {@link ProgressTracker}
 * to get both the statistics and the visual feedback.
 */
public class DebugProgressTracker implements ProgressTracker {
    private final long startTimeMillis;
    private final List<CallInfo> callInfos;
    private final String name;
    private final int expectedTotalUnits;
    private final ProgressTracker delegateTracker;
    private long lastTime;
    private int calledUnits;

    public DebugProgressTracker(String name, int expectedTotalUnits,
                                ProgressTracker delegateTracker) {
        this.name = name;
        this.expectedTotalUnits = expectedTotalUnits;
        this.delegateTracker = delegateTracker;
        startTimeMillis = System.currentTimeMillis();
        lastTime = 0;
        calledUnits = 0;
        callInfos = new ArrayList<>();
        log("created " + name + ", expectedTotalUnits = " + expectedTotalUnits);
    }

    @Override
    public void unitDone() {
        if (delegateTracker != null) {
            delegateTracker.unitDone();
        }

        calledUnits++;
        log("unitDone");
    }

    @Override
    public void unitsDone(int units) {
        if (delegateTracker != null) {
            delegateTracker.unitsDone(units);
        }

        calledUnits += units;
        log("addUnits " + units);
    }

    @Override
    public void finished() {
        if (delegateTracker != null) {
            delegateTracker.finished();
        }

        log("finish");

        System.out.print("Progress for " + name + ", received units = " + calledUnits);
        if (calledUnits == expectedTotalUnits) {
            System.out.println(", OK");
        } else {
            System.out.println(", NOK, expectedTotalUnits = " + expectedTotalUnits);
        }

        printStatisticsAboutEachCall();
        System.out.println();
    }

    private void printStatisticsAboutEachCall() {
        long totalDuration = System.currentTimeMillis() - startTimeMillis;

        callInfos.stream()
            .map(callInfo -> callInfo.asString(totalDuration))
            .map(s -> s.replace("pixelitor.", ""))
            .map(s -> s.replace("filters.", ""))
            .map(s -> s.replace("jhlabsproxies.", ""))
            .map(s -> s.replace("utils.", ""))
            .forEach(System.out::println);
    }

    private void log(String method) {
        long time = System.currentTimeMillis() - startTimeMillis;
        StackTraceElement ste = new Throwable().getStackTrace()[2];

        callInfos.add(new CallInfo(method, time, lastTime, ste, expectedTotalUnits));

        lastTime = time;
    }

    /**
     * Information about one call to a {@link DebugProgressTracker} method.
     */
    private static class CallInfo {
        private final StackTraceElement ste;
        private final String method;
        private final long time;
        private final int totalUnits;
        private final long duration;

        public CallInfo(String method, long time, long lastTime,
                        StackTraceElement ste, int totalUnits) {
            this.method = method;
            this.time = time;
            this.totalUnits = totalUnits;
            duration = time - lastTime;
            this.ste = ste;
        }

        public String asString(long totalDuration) {
            double timeSeconds = time / 1000.0;
            double durationSeconds = duration / 1000.0;

            double durationPercentage = (duration * 100.0) / totalDuration;

            String whatWithPercent = String
                .format("%s (%.1f%%=>%.2fu)", method, durationPercentage,
                    (durationPercentage / 100.0) * totalUnits);

            return String
                .format("%.2fs (dur=%.2fs): %-21s at %s.%s(%s:%d)",
                    timeSeconds, durationSeconds, whatWithPercent,
                    ste.getClassName(), ste.getMethodName(),
                    ste.getFileName(), ste.getLineNumber());
        }
    }
}
