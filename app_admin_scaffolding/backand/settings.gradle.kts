rootProject.name = "backend"

include(
    ":common",
    ":domain:entity-base",
    ":domain:entity",
    ":application:application-facade",
    ":application:security:jwt",
    ":application:api-admin-server",
    ":application:api-service-server",
    ":application:executor-server"
)