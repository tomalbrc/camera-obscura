package de.tomalbrc.cameraobscura.render.model.resource.state;

import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

public class MultipartDefinition {
    public List<Variant> apply; // models to apply

    public Condition when; // under those conditions

    public static class Condition {
        public AndCondition AND;
        public OrCondition OR;

        public Map<String, String> blockStateValues;

        public boolean canApply(BlockState blockState) {
            if (this.AND != null)
                return this.AND.canApply(blockState);
            else if (this.OR != null)
                return this.OR.canApply(blockState);
            else
                return testMap(blockState, this.blockStateValues);
        }

        protected boolean testMap(BlockState blockState, Map<String, String> map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String propertyName = entry.getKey();
                String[] valueKeys = entry.getValue().split("\\|");

                var stateDefinition = blockState.getBlock().getStateDefinition();

                var prop = stateDefinition.getProperty(propertyName);
                boolean match = false;
                for (int i = 0; i < valueKeys.length; i++) {
                    String valueKey = valueKeys[i];
                    var propVal = prop.getValue(valueKey);
                    if (!propVal.isPresent()) {
                        throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s'", valueKey, propertyName, stateDefinition.getOwner()));
                    }

                    var val = blockState.getValue(prop);
                    if (propVal.get().equals(val)) {
                        match = true;
                    }
                }

                if (!match) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class AndCondition extends Condition {
        public List<Map<String, String>> blockStateValueList;

        @Override
        public boolean canApply(BlockState blockState) {
            for (int i = 0; i < blockStateValueList.size(); i++) {
                boolean matches = testMap(blockState, blockStateValueList.get(i));
                if (!matches)
                    return false;
            }

            return true;
        }
    }

    public static class OrCondition extends Condition {
        public List<Map<String, String>> blockStateValueList;

        @Override
        public boolean canApply(BlockState blockState) {
            for (int i = 0; i < blockStateValueList.size(); i++) {
                boolean matches = testMap(blockState, blockStateValueList.get(i));
                if (matches)
                    return true;
            }

            return false;
        }
    }
}
