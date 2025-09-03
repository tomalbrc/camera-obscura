package de.tomalbrc.cameraobscura.render.model.resource.state;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MultipartDefinition {
    public List<Variant> apply;
    public Condition when;

    public static class Condition {
        public AndCondition AND;
        public OrCondition OR;
        public Map<String, String[]> blockStateValues;

        // hacky caches
        protected final ConcurrentMap<BlockState, Boolean> resultCache = new ConcurrentHashMap<>();

        private static final ConcurrentMap<Block, ConcurrentMap<String, Property<?>>> PROPERTY_CACHE = new ConcurrentHashMap<>();
        private static final ConcurrentMap<Property<?>, ConcurrentMap<String, Comparable<?>>> PROPERTY_VALUE_CACHE = new ConcurrentHashMap<>();

        public boolean canApply(BlockState blockState) {
            var cached = resultCache.get(blockState);
            if (cached != null) return cached;

            boolean result;
            if (this.AND != null)
                result = this.AND.canApply(blockState);
            else if (this.OR != null)
                result = this.OR.canApply(blockState);
            else
                result = testMap(blockState, this.blockStateValues);

            resultCache.put(blockState, result);
            return result;
        }

        protected boolean testMap(BlockState blockState, Map<String, String[]> map) {
            if (map == null || map.isEmpty()) return true;

            Block block = blockState.getBlock();
            ConcurrentMap<String, Property<?>> propMap = PROPERTY_CACHE.computeIfAbsent(block, b -> new ConcurrentHashMap<>());

            for (Map.Entry<String, String[]> entry : map.entrySet()) {
                String propertyName = entry.getKey();
                Property<?> prop = propMap.get(propertyName);
                if (prop == null) {
                    var stateDefinition = block.getStateDefinition();
                    prop = stateDefinition.getProperty(propertyName);
                    if (prop == null) {
                        throw new RuntimeException(String.format("Unknown property '%s' on '%s'", propertyName, stateDefinition.getOwner()));
                    }
                    propMap.put(propertyName, prop);
                }

                boolean match = false;
                String[] allowedValues = entry.getValue();
                if (allowedValues == null || allowedValues.length == 0) return false;

                ConcurrentMap<String, Comparable<?>> valueMap = PROPERTY_VALUE_CACHE.computeIfAbsent(prop, p -> new ConcurrentHashMap<>());

                for (String valueKey : allowedValues) {
                    Comparable<?> propVal = valueMap.get(valueKey);
                    if (propVal == null) {
                        Optional<? extends Comparable<?>> opt = prop.getValue(valueKey);
                        if (opt.isEmpty()) {
                            throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s'", valueKey, propertyName, block.getStateDefinition().getOwner()));
                        }
                        propVal = opt.get();
                        valueMap.put(valueKey, propVal);
                    }

                    var currentVal = blockState.getValue(prop);
                    if (Objects.equals(propVal, currentVal)) {
                        match = true;
                        break;
                    }
                }

                if (!match) return false;
            }

            return true;
        }
    }

    public static class AndCondition extends Condition {
        public List<Map<String, String[]>> blockStateValueList;

        @Override
        public boolean canApply(BlockState blockState) {
            Boolean cached = resultCache.get(blockState);
            if (cached != null) return cached;

            for (Map<String, String[]> map : blockStateValueList) {
                if (!testMap(blockState, map)) {
                    resultCache.put(blockState, false);
                    return false;
                }
            }

            resultCache.put(blockState, true);
            return true;
        }
    }

    public static class OrCondition extends Condition {
        public List<Map<String, String[]>> blockStateValueList;

        @Override
        public boolean canApply(BlockState blockState) {
            Boolean cached = resultCache.get(blockState);
            if (cached != null) return cached;

            for (Map<String, String[]> map : blockStateValueList) {
                if (testMap(blockState, map)) {
                    resultCache.put(blockState, true);
                    return true;
                }
            }

            resultCache.put(blockState, false);
            return false;
        }
    }
}
