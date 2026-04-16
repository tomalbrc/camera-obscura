package de.tomalbrc.cameraobscura.model.resource;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.cameraobscura.model.resource.state.MultipartDefinition;
import de.tomalbrc.cameraobscura.model.resource.state.Variant;

import java.util.List;
import java.util.Map;

public class RPBlockState {

    @SerializedName("variants")
    public Map<String, Variant> variants;

    @SerializedName("multipart")
    public List<MultipartDefinition> multipart;
}
