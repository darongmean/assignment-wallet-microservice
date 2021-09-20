_default:
    @just --list

start-jaeger:
    docker start jaeger 2>/dev/null || docker run --name jaeger -d -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 jaegertracing/all-in-one:latest

start-dev:
    ./mvnw compile quarkus:dev

open-links:
    bash -c 'sleep 5 ; open http://localhost:16686' &
    bash -c 'sleep 15 ; open http://localhost:8080' &
    bash -c 'sleep 15 ; open http://localhost:8080/q/dev' &
    bash -c 'sleep 15 ; open http://localhost:8080/q/swagger-ui' &

up: start-jaeger open-links start-dev
