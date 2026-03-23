.PHONY: up-keycloak down-keycloak stop-keycloak start-keycloak \
        up-monitoring down-monitoring stop-monitoring start-monitoring restart-monitoring \
        up-deploy down-deploy stop-deploy start-deploy \
        logs-keycloak logs-monitoring

up-keycloak:
	docker compose -f deploy/keycloak/docker-compose.yml up -d

down-keycloak:
	docker compose -f deploy/keycloak/docker-compose.yml down

stop-keycloak:
	docker compose -f deploy/keycloak/docker-compose.yml stop

start-keycloak:
	docker compose -f deploy/keycloak/docker-compose.yml start

up-monitoring:
	docker compose -f deploy/monitoring/docker-compose.yml up -d

down-monitoring:
	docker compose -f deploy/monitoring/docker-compose.yml down

stop-monitoring:
	docker compose -f deploy/monitoring/docker-compose.yml stop

start-monitoring:
	docker compose -f deploy/monitoring/docker-compose.yml start

up-deploy:
	docker compose -f deploy/keycloak/docker-compose.yml up -d
	docker compose -f deploy/monitoring/docker-compose.yml up -d

down-deploy:
	docker compose -f deploy/monitoring/docker-compose.yml down
	docker compose -f deploy/keycloak/docker-compose.yml down

stop-deploy:
	docker compose -f deploy/monitoring/docker-compose.yml stop
	docker compose -f deploy/keycloak/docker-compose.yml stop

start-deploy:
	docker compose -f deploy/keycloak/docker-compose.yml start
	docker compose -f deploy/monitoring/docker-compose.yml start

restart-monitoring:
	docker compose -f deploy/monitoring/docker-compose.yml restart

logs-keycloak:
	docker compose -f deploy/keycloak/docker-compose.yml logs -f

logs-monitoring:
	docker compose -f deploy/monitoring/docker-compose.yml logs -f