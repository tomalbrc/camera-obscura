package de.tomalbrc.cameraobscura.renderer.animation;

import java.util.*;

public class AnimationDefinition {
    public final double length;
    public final boolean looping;
    public final Map<String, List<AnimationChannel>> channels = new HashMap<>();

    private AnimationDefinition(double length, boolean looping) {
        this.length = length;
        this.looping = looping;
    }

    public static Builder withLength(double length) {
        return new Builder(length);
    }

    public List<AnimationChannel> getChannels(String partName) {
        return channels.getOrDefault(partName, Collections.emptyList());
    }

    public static class Builder {
        private final double length;
        private final Map<String, List<AnimationChannel>> channels = new HashMap<>();
        private boolean looping = false;

        public Builder(double length) {
            this.length = length;
        }

        public static Builder withLength(double length) {
            return new Builder(length);
        }

        public Builder looping() {
            this.looping = true;
            return this;
        }

        public Builder addAnimation(String part, AnimationChannel channel) {
            channels.computeIfAbsent(part, k -> new ArrayList<>()).add(channel);
            return this;
        }

        public AnimationDefinition build() {
            AnimationDefinition def = new AnimationDefinition(length, looping);
            def.channels.putAll(channels);
            return def;
        }
    }
}
