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

### Class & Infrastructure Overview

The diagram below maps every module — frontend types, API controllers, DTOs, services, JPA repositories, domain entities, and infrastructure — and shows how they depend on each other.

```mermaid
flowchart LR
    classDef webCls    fill:#0891B2,stroke:#164E63,color:#fff,font-weight:bold
    classDef webTyp    fill:#A5F3FC,stroke:#0891B2,color:#164E63
    classDef ctrlCls   fill:#7C3AED,stroke:#4C1D95,color:#fff,font-weight:bold
    classDef dtoCls    fill:#DDD6FE,stroke:#7C3AED,color:#2e1065
    classDef svcCls    fill:#EA580C,stroke:#7C2D12,color:#fff,font-weight:bold
    classDef repoCls   fill:#15803D,stroke:#14532D,color:#fff,font-weight:bold
    classDef entCls    fill:#166534,stroke:#14532D,color:#fff
    classDef enumCls   fill:#BBF7D0,stroke:#15803D,color:#14532D
    classDef infraCls  fill:#B91C1C,stroke:#7F1D1D,color:#fff,font-weight:bold
    classDef cacheCls  fill:#DC2626,stroke:#7F1D1D,color:#fff
    classDef k8sCls    fill:#D97706,stroke:#78350F,color:#fff

    %% ─── Web Module ────────────────────────────────────────
    subgraph web["🌐  Web Module · React + TypeScript (Vite)"]
        App["App\n«root component»"]:::webCls
        subgraph webTypes["Types / Interfaces"]
            TaxNeedT["TaxNeed\n«type»"]:::webTyp
            TimelineT["Timeline\n«type»"]:::webTyp
            ProviderT["Provider\n«interface»"]:::webTyp
            MatchT["Match\n«interface»"]:::webTyp
            LeadRespT["LeadResponse\n«interface»"]:::webTyp
        end
    end

    %% ─── API Module ─────────────────────────────────────────
    subgraph apimod["🔌  API Module · Controllers + DTOs"]
        LC["LeadController\n@RestController\nPOST /api/leads\nGET  /api/leads/{id}"]:::ctrlCls
        PC["ProviderController\n@RestController\nGET /api/providers"]:::ctrlCls
        subgraph dtos["DTOs «record»"]
            CLR["CreateLeadRequest"]:::dtoCls
            LRsp["LeadResponse"]:::dtoCls
            MRsp["MatchResponse"]:::dtoCls
            PRsp["ProviderResponse"]:::dtoCls
        end
    end

    %% ─── Service Module ──────────────────────────────────────
    subgraph svcmod["⚙️  Service Module · Business Logic"]
        LS["LeadService\n@Service · @Transactional"]:::svcCls
        PMS["ProviderMatchingService\n@Service · @Cacheable"]:::svcCls
    end

    %% ─── Data Module ─────────────────────────────────────────
    subgraph datamod["🗃️  Data Module · JPA Repositories + Domain"]
        subgraph repos["«JpaRepository» interfaces"]
            LRepo["LeadRepository"]:::repoCls
            PRepo["ProviderRepository"]:::repoCls
            LMRepo["LeadMatchRepository"]:::repoCls
        end
        subgraph entities["@Entity classes"]
            LeadE["Lead"]:::entCls
            ProvE["Provider"]:::entCls
            LME["LeadMatch"]:::entCls
        end
        subgraph enums["Enums"]
            TNE["TaxNeed"]:::enumCls
            TLE["Timeline"]:::enumCls
            LSE["LeadStatus"]:::enumCls
        end
    end

    %% ─── Infrastructure ──────────────────────────────────────
    subgraph infra["🏗️  Infrastructure"]
        PG[("🐘 PostgreSQL\n:5432")]:::infraCls
        RD[("⚡ Redis\n:6379")]:::cacheCls
        ING["🌐 Nginx Ingress\ntax-trusted.example.com"]:::k8sCls
        K8S["☸️ K8s Services\nweb · api · redis · postgres"]:::k8sCls
    end

    %% ── Flows ───────────────────────────────────────────────
    App -->|"POST /api/leads"| LC
    App -->|"GET /api/providers"| PC

    LC --> CLR
    LC --> LRsp
    LRsp --> MRsp
    MRsp --> PRsp

    LC --> LS
    PC --> PMS
    LS --> PMS

    LS  --> LRepo
    LS  --> LMRepo
    PMS --> PRepo

    LRepo  -.->|manages| LeadE
    PRepo  -.->|manages| ProvE
    LMRepo -.->|manages| LME

    LME -->|ManyToOne| LeadE
    LME -->|ManyToOne| ProvE
    LeadE -->|uses| TNE
    LeadE -->|uses| TLE
    LeadE -->|uses| LSE
    ProvE -->|uses| TNE

    LRepo  -->|JPA / JDBC| PG
    PRepo  -->|JPA / JDBC| PG
    LMRepo -->|JPA / JDBC| PG
    PMS    -->|"@Cacheable"| RD

    TaxNeedT  -. mirrors .-> TNE
    TimelineT -. mirrors .-> TLE

    ING -->|"/* → web:80"| K8S
    ING -->|"/api/* → api:8080"| K8S

    style web      fill:#ECFEFF,stroke:#0891B2,color:#164E63
    style webTypes fill:#CFFAFE,stroke:#0891B2,color:#164E63
    style apimod   fill:#F5F3FF,stroke:#7C3AED,color:#2e1065
    style dtos     fill:#EDE9FE,stroke:#7C3AED,color:#2e1065
    style svcmod   fill:#FFF7ED,stroke:#EA580C,color:#431407
    style datamod  fill:#F0FDF4,stroke:#15803D,color:#14532D
    style repos    fill:#DCFCE7,stroke:#15803D,color:#14532D
    style entities fill:#BBF7D0,stroke:#166534,color:#14532D
    style enums    fill:#F0FDF4,stroke:#86EFAC,color:#14532D
    style infra    fill:#FEF2F2,stroke:#B91C1C,color:#7F1D1D
```

---

### GCP Production Deployment

The diagram below is the recommended **GCP production deployment** for this repo. It is slightly more detailed than the current manifests: the repo already has Kubernetes objects for `web`, `api`, `redis`, `postgres`, and `ingress`, and this diagram shows how those map to Google Cloud managed services and edge networking in a production setup.

```mermaid
flowchart LR
    classDef userCls    fill:#3B82F6,stroke:#1D4ED8,color:#fff,font-weight:bold
    classDef dnsCls     fill:#60A5FA,stroke:#2563EB,color:#fff
    classDef edgeCls    fill:#F97316,stroke:#C2410C,color:#fff,font-weight:bold
    classDef secCls     fill:#EF4444,stroke:#B91C1C,color:#fff
    classDef gatewayCls fill:#7C3AED,stroke:#5B21B6,color:#fff
    classDef webCls     fill:#059669,stroke:#047857,color:#fff
    classDef apiCls     fill:#8B5CF6,stroke:#6D28D9,color:#fff
    classDef hpaCls     fill:#C4B5FD,stroke:#7C3AED,color:#1e1b4b
    classDef platformCls fill:#F59E0B,stroke:#B45309,color:#1c1917
    classDef dataCls    fill:#10B981,stroke:#065F46,color:#fff
    classDef buildCls   fill:#0D9488,stroke:#134E4A,color:#fff

    user["👤 Users / Browsers"]:::userCls --> dns["☁️ 1. Cloud DNS<br/>tax-trusted.example.com"]:::dnsCls
    dns --> edge["⚡ 2. Global External Application Load Balancer<br/>Anycast IP · Google Front Ends"]:::edgeCls

    subgraph edge_sec["🛡️ 3. Edge Security + Gateway"]
        armor["🔒 Cloud Armor<br/>WAF + DDoS protection"]:::secCls
        certs["🔑 Certificate Manager<br/>Google-managed TLS"]:::secCls
        gateway["🌐 GKE Gateway API / Ingress<br/>Path routing rules"]:::gatewayCls
    end

    edge --> armor
    armor --> certs
    certs --> gateway

    subgraph gke["☸️ 4. GKE Regional Cluster (multi-zone)"]
        websvc["web Service"]:::webCls
        apisvc["api Service"]:::apiCls

        subgraph webtier["🌍 Web Tier"]
            webpods["📦 Web Deployment<br/>Nginx · Vite static build<br/>2+ replicas"]:::webCls
            webhpa["📈 Horizontal Scaling<br/>Replica growth under load"]:::hpaCls
        end

        subgraph apitier["⚙️ API Tier"]
            apipods["🚀 API Deployment<br/>Spring Boot · lead + matching<br/>2+ replicas"]:::apiCls
            apihpa["📊 HPA<br/>CPU / memory scaling"]:::hpaCls
            wi["🔐 Workload Identity<br/>Least-privilege GCP access"]:::secCls
        end

        subgraph platform["🏗️ Cluster Platform"]
            negs["⚖️ Container-native LB<br/>NEGs per Service"]:::platformCls
            ca["🔄 Cluster Autoscaler<br/>Autopilot capacity"]:::platformCls
            logs["📋 Cloud Logging<br/>+ Monitoring"]:::platformCls
        end
    end

    gateway -->|"/*"| websvc
    gateway -->|"/api/*"| apisvc
    websvc --> webpods
    apisvc --> apipods
    webpods --> negs
    apipods --> negs
    webhpa --> webpods
    apihpa --> apipods
    ca --> webpods
    ca --> apipods
    logs --> webpods
    logs --> apipods
    wi --> apipods

    subgraph data["🗄️ 5. Managed Data Services"]
        sql["🐘 Cloud SQL for PostgreSQL<br/>Private IP in VPC"]:::dataCls
        redis["⚡ Memorystore for Redis<br/>Private service endpoint"]:::dataCls
        backups["💾 Automated Backups + HA"]:::dataCls
    end

    apipods -->|"JDBC · private IP"| sql
    apipods -->|"Redis client · private IP"| redis
    backups --> sql
    backups --> redis

    subgraph delivery["🚀 6. Build + Release Path"]
        git["🐙 GitHub<br/>Source Repo"]:::buildCls
        build["🔨 Cloud Build<br/>GitHub Actions"]:::buildCls
        registry["📦 Artifact Registry<br/>Web + API images"]:::buildCls
    end

    git --> build
    build --> registry
    registry --> webpods
    registry --> apipods

    style edge_sec  fill:#FEE2E2,stroke:#EF4444,color:#450a0a
    style gke       fill:#EDE9FE,stroke:#7C3AED,color:#2e1065
    style webtier   fill:#D1FAE5,stroke:#059669,color:#022c22
    style apitier   fill:#F3E8FF,stroke:#8B5CF6,color:#2e1065
    style platform  fill:#FEF3C7,stroke:#F59E0B,color:#1c1917
    style data      fill:#D1FAE5,stroke:#10B981,color:#022c22
    style delivery  fill:#CCFBF1,stroke:#0D9488,color:#042f2e
```

### How Traffic Flows

1. Users resolve `tax-trusted.example.com` in Cloud DNS.
2. Traffic lands on a **global external Application Load Balancer**, which uses Google’s edge to terminate HTTPS close to the user.
3. **Cloud Armor** applies WAF and DDoS controls before traffic reaches the cluster.
4. The load balancer forwards traffic into **GKE Gateway / Ingress** rules.
5. Routing is path-based:
   - `/` goes to the `tax-trusted-web` Service.
   - `/api/*` goes to the `tax-trusted-api` Service.
6. GKE uses **container-native load balancing** with NEGs so the load balancer targets Pods directly rather than only nodes.
7. Inside the cluster, the `web` pods serve the static frontend and the `api` pods process lead creation, provider matching, and reads.
8. The API connects privately to **Cloud SQL for PostgreSQL** and **Memorystore for Redis** over the VPC.

### How The Gateway Works On GCP

- Best-fit option: **GKE Gateway API** with `gke-l7-global-external-managed`.
- Why: Google recommends the managed global external Gateway classes when you want the newer external Application Load Balancer features.
- Front door behavior:
  - One global Anycast IP.
  - TLS terminated at the Google edge.
  - Host and path rules map requests to Kubernetes Services.
- App mapping for this repo:
  - `tax-trusted.example.com/` -> `tax-trusted-web`
  - `tax-trusted.example.com/api/*` -> `tax-trusted-api`

### How Load Balancing Works

- **Layer 1: Global edge balancing**
  - Google Front Ends accept traffic on the global Anycast IP.
  - Requests are sent over Google’s backbone to healthy backends.
- **Layer 2: Service-to-pod balancing**
  - GKE exposes Services through NEGs so the load balancer can send traffic to individual Pods.
  - Health checks determine which backends are eligible.
- **Layer 3: Kubernetes scaling**
  - HPA scales pod replicas up and down.
  - Cluster autoscaler, or Autopilot capacity management, adds compute when pods can’t be scheduled.

### GCP Deployment Detail

- **Compute**
  - GKE regional cluster across multiple zones for higher availability.
  - Separate Deployments for `web` and `api`.
  - `api` should keep readiness and liveness probes, which the repo already defines.
- **Networking**
  - Cloud DNS for the public hostname.
  - Global external Application Load Balancer at the edge.
  - Gateway API or GKE Ingress for HTTP routing into the cluster.
  - Private VPC connectivity to data services.
- **Data**
  - Cloud SQL for PostgreSQL is the production replacement for the in-cluster Postgres StatefulSet.
  - Memorystore for Redis is the production replacement for the in-cluster Redis Deployment.
  - Private IP is preferred for both.
- **Security**
  - Cloud Armor for edge protection.
  - Certificate Manager for managed TLS.
  - Workload Identity for GKE so pods use IAM without long-lived service account keys.
  - Secrets should move from plain Kubernetes secrets to Secret Manager integration if you want a stronger production posture.
- **Operations**
  - Cloud Logging and Cloud Monitoring for telemetry.
  - HPA for pods and cluster autoscaler for node capacity.
  - Artifact Registry for container images.
  - Cloud Build or GitHub Actions for build and deploy automation.

### Mapping From This Repo To GCP

- [`k8s/web.yaml`](/Users/juancabral/Documents/New%20project%202/tax-trusted/k8s/web.yaml): maps to the GKE `web` Deployment and Service.
- [`k8s/api.yaml`](/Users/juancabral/Documents/New%20project%202/tax-trusted/k8s/api.yaml): maps to the GKE `api` Deployment, Service, probes, and HPA.
- [`k8s/ingress.yaml`](/Users/juancabral/Documents/New%20project%202/tax-trusted/k8s/ingress.yaml): maps to the external HTTP entry point; on GCP I would evolve this toward Gateway API.
- [`k8s/postgres.yaml`](/Users/juancabral/Documents/New%20project%202/tax-trusted/k8s/postgres.yaml): fine for demos, but in GCP production I’d replace it with Cloud SQL.
- [`k8s/redis.yaml`](/Users/juancabral/Documents/New%20project%202/tax-trusted/k8s/redis.yaml): fine for demos, but in GCP production I’d replace it with Memorystore.

### Local vs Hosted

- `docker compose` is still the fastest local setup: `web`, `api`, `postgres`, and `redis` run on your machine.
- The **hosted GCP version** uses the same app split, but moves ingress, load balancing, TLS, autoscaling, and managed data onto Google Cloud services.

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
