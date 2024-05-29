package am.ik.spring.ecs.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import co.elastic.logging.logback.EcsEncoder;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

public class EcsEncoderConfigurationListener implements GenericApplicationListener {

	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		if (eventType.getRawClass() == null) {
			return false;
		}
		return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType.getRawClass());
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return SpringApplication.class.isAssignableFrom(sourceType)
				|| ApplicationContext.class.isAssignableFrom(sourceType);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (!ClassUtils.isPresent("ch.qos.logback.classic.LoggerContext", null)
				|| !ClassUtils.isPresent("co.elastic.logging.logback.EcsEncoder", null)) {
			return;
		}
		if (event instanceof ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent) {
			ILoggerFactory loggerFactorySpi = LoggerFactory.getILoggerFactory();
			if (!(loggerFactorySpi instanceof LoggerContext loggerContext)) {
				return;
			}
			Binder binder = Binder.get(applicationEnvironmentPreparedEvent.getEnvironment());
			EcsEncoder ecsEncoder = new EcsEncoder();
			this.configureEcsEncoder(ecsEncoder, binder);
			loggerContext.getLoggerList().forEach(logger -> logger.iteratorForAppenders().forEachRemaining(appender -> {
				if (appender instanceof OutputStreamAppender<ILoggingEvent>) {
					((OutputStreamAppender<ILoggingEvent>) appender).setEncoder(ecsEncoder);
				}
			}));
		}
	}

	void configureEcsEncoder(EcsEncoder ecsEncoder, Binder binder) {
		binder.bind("spring.application.name", String.class).ifBound(ecsEncoder::setServiceName);
	}

	@Override
	public int getOrder() {
		// After org.springframework.boot.context.logging.LoggingApplicationListener
		return LoggingApplicationListener.DEFAULT_ORDER + 2;
	}

}
