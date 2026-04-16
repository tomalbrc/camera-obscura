-ignorewarnings
-dontnote
-dontwarn **

# only rename
-dontshrink
-dontoptimize

# keep attr
-keepattributes *Annotation*
-keepattributes Signature

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    ** $VALUES;
}

-adaptresourcefilecontents **.json, **.toml

-keep class de.tomalbrc.cameraobscura.CameraObscuraPlugin { *; }
-keep class de.tomalbrc.cameraobscura.CameraObscura { *; }

-keep @org.spongepowered.asm.mixin.Mixin class * { *; }
-keepclassmembers class * {
    @org.spongepowered.asm.mixin.Mixin <methods>;
}

-keep class dev.lone.** { *; }
-keep class com.nexomc.** { *; }

-keep class io.papermc.** { *; }
-keep class org.bukkit.** { *; }

-keep class net.minecraft.** { *; }
-keep class com.mojang.** { *; }
-keep class net.fabricmc.** { *; }
-keep class com.google.gson.** { *; }
