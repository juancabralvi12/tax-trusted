create table providers (
  id bigserial primary key,
  name varchar(160) not null,
  firm_name varchar(180) not null,
  zip_code varchar(10) not null,
  city varchar(120) not null,
  state varchar(2) not null,
  bio text not null,
  rating numeric(3,2) not null default 0,
  average_response_minutes integer not null default 1440,
  weekly_capacity integer not null default 20,
  active boolean not null default true,
  created_at timestamptz not null default now()
);

create table provider_specialties (
  provider_id bigint not null references providers(id) on delete cascade,
  specialty varchar(64) not null,
  primary key (provider_id, specialty)
);

create table leads (
  id uuid primary key,
  zip_code varchar(10) not null,
  first_name varchar(120),
  last_name varchar(120),
  email varchar(180),
  phone varchar(40),
  timeline varchar(40) not null,
  status varchar(40) not null,
  created_at timestamptz not null default now()
);

create table lead_needs (
  lead_id uuid not null references leads(id) on delete cascade,
  need varchar(64) not null,
  primary key (lead_id, need)
);

create table lead_matches (
  id bigserial primary key,
  lead_id uuid not null references leads(id) on delete cascade,
  provider_id bigint not null references providers(id),
  score integer not null,
  rank integer not null,
  score_reason text not null,
  created_at timestamptz not null default now(),
  unique (lead_id, provider_id)
);

create index idx_providers_active_zip on providers(active, zip_code);
create index idx_providers_rating_capacity on providers(active, rating desc, weekly_capacity desc);
create index idx_provider_specialties_specialty on provider_specialties(specialty, provider_id);
create index idx_leads_status_created on leads(status, created_at desc);
create index idx_leads_zip_created on leads(zip_code, created_at desc);
create index idx_lead_matches_lead_rank on lead_matches(lead_id, rank);

