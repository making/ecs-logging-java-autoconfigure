# AutoConfiguration for ECS logging for Java

```xml
		<dependency>
			<groupId>am.ik.spring.ecs</groupId>
			<artifactId>logback-ecs-encoder-autoconfigure</artifactId>
			<version>0.1.3</version>
		</dependency>
```

`EcsEncoder` will be automatically added to each `OutputStreamAppender`. No additional config is required like `logback-spring.xml`.

You can also disable `EcsEncoder` at the runtime by `logging.logback.ecs-encoder.enabled=false`
