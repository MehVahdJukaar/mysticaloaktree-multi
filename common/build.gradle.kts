plugins {
    id("com.possible-triangle.common")
}

common {
    neoformVersion = "1.21.1-20240808.144430"
    accessWidener()
}

val moonlight_version: String by extra

dependencies {
    modCompileOnly("net.mehvahdjukaar:moonlight-common:${moonlight_version}")
    //moonlight AT, needed for CreativeModeTabs keys
    accessTransformers("net.mehvahdjukaar:moonlight-common:${moonlight_version}")
}
