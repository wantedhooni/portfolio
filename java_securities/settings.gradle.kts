rootProject.name = "securities_monolithic"

include(
    ":common",
    ":domain-jpa",
    ":domain-processor",
    ":external-api:yfinance-client",
    ":api-server"
)

