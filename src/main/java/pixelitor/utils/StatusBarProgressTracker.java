/*
 * Copyright 2020 Laszlo Balazs-Csiki and Contributors
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

/**
 * Tracks the progress of some operation and shows a
 * status bar update if it takes a long time.
 */
public class StatusBarProgressTracker extends ThresholdProgressTracker {
    private final String name;
    private ProgressHandler progressHandler;

    public StatusBarProgressTracker(String name, int numComputationUnits) {
        super(numComputationUnits);
        assert name != null;

        this.name = name + ":";
    }

    @Override
    void startProgressTracking() {
        progressHandler = Messages.startProgress(name, 100);
    }

    @Override
    void updateProgressTracking(int percent) {
        progressHandler.updateProgress(percent);
    }

    @Override
    void finishProgressTracking() {
        progressHandler.stopProgress();
    }
}
