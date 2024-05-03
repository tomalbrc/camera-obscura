package de.tomalbrc.cameraobscura.render.model.resource.state;

import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

public class MultipartDefinition {
    public Variant apply; // model to apply

    public Condition when; // under those conditions

    public Variant get(BlockState blockState) {
        if (this.when.AND != null) this.when.AND.forEach(x -> {
            //x.
        });

        return null;
    }

    public static class Condition {
        public List<Condition> OR;
        public List<Condition> AND;

        public Map<String, String> blockStateValues;

        public boolean canApply(BlockState blockState) {
            for (Map.Entry<String, String> entry : blockStateValues.entrySet()) {
                String propertyName = entry.getKey();
                String valueKey = entry.getValue();

                var stateDefinition = blockState.getBlock().getStateDefinition();

                var prop = stateDefinition.getProperty(propertyName);
                var propVal = prop.getValue(valueKey);
                if (!propVal.isPresent()) {
                    throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s'", valueKey, propertyName, stateDefinition.getOwner()));
                }

                var val = blockState.getValue(prop);
                if (val.equals(propVal)) {
                    return true;
                }
            }

            return false;
        }
    }
}
