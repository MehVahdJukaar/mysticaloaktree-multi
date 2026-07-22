plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val moonlight_version: String by extra

dependencies {
    modCompileOnly("net.mehvahdjukaar:moonlight-common:${moonlight_version}")
    // Moonlight's AT widens vanilla members the common code touches (e.g. CreativeModeTabs keys)
    accessTransformers("net.mehvahdjukaar:moonlight-common:${moonlight_version}")
}
