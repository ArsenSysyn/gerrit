load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "metrics",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//java/com/google/gerrit/common:server",
        "//java/com/google/gerrit/extensions:api",
        "//java/com/google/gerrit/lifecycle",
        "//java/com/google/gerrit/server/cancellation",
        "//java/com/google/gerrit/server/logging",
        "//lib:guava",
        "//lib:jgit",
        "//lib/auto:auto-value",
        "//lib/auto:auto-value-annotations",
        "//lib/flogger:api",
        "//lib/guice",
    ],
)
