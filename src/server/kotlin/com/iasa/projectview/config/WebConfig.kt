package com.iasa.projectview.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

	override fun addViewControllers(registry: ViewControllerRegistry) {
		val viewName = "forward:/"
		registry.addViewController("/{spring:\\w+}")
			.setViewName(viewName)
		registry.addViewController("/**/{spring:\\w+}")
			.setViewName(viewName)
		registry.addViewController("/{spring:\\w+}/**{spring:?!(\\.js|\\.css)$}")
			.setViewName(viewName)
	}
}
