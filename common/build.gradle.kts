plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val moonlight_version: String by extra

dependencies {
    // Use the neoforge variant in common for compile-only access
    modCompileOnly("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    // Moonlight's AT widens vanilla members the common code touches (e.g. CreativeModeTabs keys)
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
}
