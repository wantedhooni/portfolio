package com.revy.example

import io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME
import io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.revy.example"
)
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = """
        pretty,
        json:build/cucumber-reports/cucumber.json,
        html:build/cucumber-reports/cucumber.html
        """
)
class CucumberTestSuite