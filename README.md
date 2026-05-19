# Tax Trusted

A full-stack reference implementation for a RamseyTrusted-style tax provider marketplace.

The product flow is simple:

1. A visitor enters their ZIP code.
2. They select what kind of tax help they need.
3. The backend creates a lead.
4. The matching service scores providers by specialty, service area, rating, capacity, and response speed.
5. The frontend shows recommended tax pros and the lead status.

## Stack

- Frontend: React, TypeScript, Vite
- Backend: Java 21, Spring Boot, Spring Web, Spring Data JPA
- Database: PostgreSQL with Flyway migrations and query indexes
- Cache: Redis for provider search/matching lookups
- Ops: Docker Compose, Kubernetes manifests, HPA, readiness/liveness probes

## Architecture

```mermaid
flowchart LR
    user["Visitor / Buyer"] --> browser["Browser"]

    subgraph exp["1. Experience Layer"]
        browser --> web["Tax Trusted Web App<br/>React + TypeScript + Vite<br/>Served by Nginx"]
        web --> flow["Lead Intake Flow<br/>ZIP + Need + Timeline"]
        web --> results["Matched Provider Results"]
    end

    subgraph core["2. Application Core"]
        api["Tax Trusted API<br/>Spring Boot"] --> lead["Lead Service<br/>Creates and reads leads"]
        api --> matching["Provider Matching Service<br/>Ranks providers by fit"]
        lead --> leadRepo["Lead Repository"]
        lead --> matchRepo["Lead Match Repository"]
        matching --> providerRepo["Provider Repository"]
    end

    subgraph data["3. Data + Platform Services"]
        db["PostgreSQL<br/>Leads, Providers, Matches"]
        redis["Redis Cache<br/>Cached provider candidates"]
        flyway["Flyway Migrations<br/>Schema + seed data"]
    end

    subgraph deploy["4. Deployable Units"]
        webImage["Web Container Image<br/>Node build -> Nginx runtime"]
        apiImage["API Container Image<br/>Maven build -> Java runtime"]
    end

    web -->|"POST /api/leads<br/>GET /api/leads/{id}<br/>GET /api/providers"| api
    leadRepo --> db
    matchRepo --> db
    providerRepo --> db
    matching --> redis
    flyway --> db
    webImage -. packages .-> web
    apiImage -. packages .-> api
```

At a high level, the browser talks to the web app, the web app calls the Spring Boot API, and the API persists leads and matches in PostgreSQL while using Redis to cache reusable provider candidate lookups. Flyway applies schema and seed data as the backend starts.

The matching flow is synchronous in this reference app: a lead is saved first, candidate providers are fetched and scored, the top matches are stored, and the response is returned to the frontend for display.

## Deployment Topology

```mermaid
flowchart TB
    subgraph local["Local Development with Docker Compose"]
        localUser["Developer Browser<br/>localhost:5173"] --> localWeb["web service<br/>Nginx container"]
        localWeb -->|"API calls"| localApi["api service<br/>Spring Boot container<br/>localhost:8080"]
        localApi --> localRedis["redis service<br/>localhost:6379"]
        localApi --> localDb["postgres service<br/>localhost:5432"]
    end

    subgraph k8s["Kubernetes Deployment"]
        internet["User Browser"] --> ingress["Ingress<br/>tax-trusted.example.com"]
        ingress --> webSvc["tax-trusted-web Service"]
        ingress --> apiSvc["tax-trusted-api Service"]

        webSvc --> webPods["tax-trusted-web Deployment<br/>2 replicas"]
        apiSvc --> apiPods["tax-trusted-api Deployment<br/>2 replicas + HPA"]

        apiPods --> redisSvc["redis Service"]
        apiPods --> pgSvc["postgres Service"]

        redisSvc --> redisPod["redis Deployment<br/>1 replica"]
        pgSvc --> pgSet["postgres StatefulSet<br/>1 replica + PVC"]

        config["ConfigMap + Secret"] --> apiPods
    end
```

The repo supports two deployment modes:

- `docker compose` for local development, where every service runs on your machine and the web app is exposed at `http://localhost:5173`.
- Kubernetes for hosted environments, where traffic enters through the NGINX Ingress on `tax-trusted.example.com`, routes to separate web and API Services, and the API scales horizontally with an HPA while Postgres keeps persistent storage through a StatefulSet volume claim.

## Run Locally

```bash
cd tax-trusted
docker compose up --build
```

Then open:

- Web: http://localhost:5173
- API: http://localhost:8080
- Health: http://localhost:8080/actuator/health

## API

```http
POST /api/leads
GET /api/leads/{id}
GET /api/providers?zipCode=37067&need=PERSONAL_TAXES
```

Example lead:

```json
{
  "zipCode": "37067",
  "needs": ["PERSONAL_TAXES", "SMALL_BUSINESS_TAXES"],
  "timeline": "THIS_MONTH",
  "firstName": "Juan",
  "lastName": "Cabral",
  "email": "juan@example.com",
  "phone": "555-555-5555"
}
```

## Scaling Notes

- Provider queries are indexed by active status and ZIP code.
- Lead rows are indexed by status and creation date for operations dashboards.
- Redis caches repeated ZIP/specialty provider searches.
- Matching is isolated in `ProviderMatchingService` so scoring can evolve independently.
- API pods include readiness/liveness probes and horizontal autoscaling.
- Frontend and backend are separate deployables, so traffic can scale independently.
# tax-trusted
