insert into providers (name, firm_name, zip_code, city, state, bio, rating, average_response_minutes, weekly_capacity)
values
  ('Megan Price', 'Price Tax Advisory', '37067', 'Franklin', 'TN', 'CPA focused on family tax planning, self-employment income, and clear year-round guidance.', 4.9, 120, 18),
  ('Caleb Warren', 'Warren Small Business Tax', '37203', 'Nashville', 'TN', 'Small-business tax pro helping owners clean up books, estimate quarterly payments, and file confidently.', 4.8, 90, 12),
  ('Nora Patel', 'Patel Tax Partners', '37064', 'Franklin', 'TN', 'Experienced tax advisor for personal returns, rental properties, and multi-state filings.', 4.7, 240, 20),
  ('Luis Romero', 'Romero Business Services', '37135', 'Nolensville', 'TN', 'Supports contractors, creators, and service businesses with bookkeeping-aware tax strategy.', 4.6, 180, 10),
  ('Grace Miller', 'Miller Family Tax', '90210', 'Beverly Hills', 'CA', 'Personal tax specialist for families, itemized deductions, and stock compensation questions.', 4.8, 150, 15);

insert into provider_specialties (provider_id, specialty)
select id, 'PERSONAL_TAXES' from providers where name in ('Megan Price', 'Nora Patel', 'Grace Miller');

insert into provider_specialties (provider_id, specialty)
select id, 'SELF_EMPLOYMENT_TAXES' from providers where name in ('Megan Price', 'Caleb Warren', 'Luis Romero');

insert into provider_specialties (provider_id, specialty)
select id, 'SMALL_BUSINESS_TAXES' from providers where name in ('Caleb Warren', 'Luis Romero', 'Nora Patel');

insert into provider_specialties (provider_id, specialty)
select id, 'BACK_TAXES' from providers where name in ('Nora Patel', 'Megan Price');

