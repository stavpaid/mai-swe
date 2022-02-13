/*
 * Copyright 2019 Laszlo Balazs-Csiki and Contributors
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

import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;

/**
 * Tracks the reading of a large file
 * with the help of a {@link ProgressTracker}
 */
public class TrackerReadProgressListener implements IIOReadProgressListener {
    private final ProgressTracker tracker;
    private int workDone = 0;

    public TrackerReadProgressListener(ProgressTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void sequenceStarted(ImageReader source, int minIndex) {

    }

    @Override
    public void sequenceComplete(ImageReader source) {

    }

    @Override
    public void imageStarted(ImageReader source, int imageIndex) {
    }

    @Override
    public void imageProgress(ImageReader source, float percentageDone) {
        int progress = (int) percentageDone;
        if (progress > workDone) {
            tracker.unitsDone(progress - workDone);
            workDone = progress;
        }
    }

    @Override
    public void imageComplete(ImageReader source) {
        tracker.finished();
    }

    @Override
    public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {

    }

    @Override
    public void thumbnailProgress(ImageReader source, float percentageDone) {

    }

    @Override
    public void thumbnailComplete(ImageReader source) {

    }

    @Override
    public void readAborted(ImageReader source) {

    }
}
