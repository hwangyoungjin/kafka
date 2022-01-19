## Reactor Kafka through spring boot Webflux
- [kafak 공식문서](https://projectreactor.io/docs/kafka/milestone/api/overview-tree.html)
---
### Environment
```yaml
1. jdk17
2. spring-boot-starter-webflux
```

### dependency
```grdlew
// https://mvnrepository.com/artifact/io.projectreactor.kafka/reactor-kafka
    implementation ("io.projectreactor.kafka:reactor-kafka:1.0.0.M1")

// inject (@Named 사용을 위함)
    implementation("javax.inject:javax.inject:1")

```