package com.aiyaapp.aiya.gpuImage;

import java.util.ArrayList;

public class AYGPUImageOutput {

    private ArrayList<AYGPUImageInput> targets = new ArrayList();

    protected ArrayList<AYGPUImageInput> getTargets() {
        return targets;
    }

    public void addTarget(AYGPUImageInput newTarget) {
        if (targets.contains(newTarget)) {
            return;
        }

        targets.add(newTarget);
    }

    public void removeTarget(AYGPUImageInput targetToRemove) {
        if (!targets.contains(targetToRemove)) {
            return;
        }

        targets.remove(targetToRemove);
    }

    public void removeAllTargets() {
        targets.clear();
    }
}