import React, { useState } from "react";
import { createRoot } from "react-dom/client";
import { ArrowRight, BadgeCheck, Clock, MapPin, ShieldCheck, Sparkles } from "lucide-react";
import "./styles.css";

type TaxNeed = "PERSONAL_TAXES" | "SELF_EMPLOYMENT_TAXES" | "SMALL_BUSINESS_TAXES" | "BACK_TAXES";
type Timeline = "ASAP" | "THIS_MONTH" | "THIS_QUARTER" | "JUST_RESEARCHING";

type Provider = {
  id: number;
  name: string;
  firmName: string;
  city: string;
  state: string;
  zipCode: string;
  bio: string;
  rating: number;
  averageResponseMinutes: number;
  weeklyCapacity: number;
  specialties: TaxNeed[];
};

type Match = {
  rank: number;
  score: number;
  scoreReason: string;
  provider: Provider;
};

type LeadResponse = {
  id: string;
  zipCode: string;
  needs: TaxNeed[];
  timeline: Timeline;
  status: string;
  matches: Match[];
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const needs: Array<{ value: TaxNeed; label: string; description: string }> = [
  { value: "PERSONAL_TAXES", label: "Personal Taxes", description: "W-2s, deductions, investments, and family returns." },
  { value: "SELF_EMPLOYMENT_TAXES", label: "Self-Employment Taxes", description: "1099 income, quarterly payments, and expenses." },
  { value: "SMALL_BUSINESS_TAXES", label: "Small-Business Taxes", description: "LLCs, S-Corps, payroll, and owner strategy." },
  { value: "BACK_TAXES", label: "Back Taxes", description: "Late filings, notices, and catch-up planning." },
];

const timelines: Array<{ value: Timeline; label: string }> = [
  { value: "ASAP", label: "ASAP" },
  { value: "THIS_MONTH", label: "This month" },
  { value: "THIS_QUARTER", label: "This quarter" },
  { value: "JUST_RESEARCHING", label: "Just researching" },
];

function App() {
  const [zipCode, setZipCode] = useState("37067");
  const [selectedNeeds, setSelectedNeeds] = useState<TaxNeed[]>(["PERSONAL_TAXES"]);
  const [timeline, setTimeline] = useState<Timeline>("THIS_MONTH");
  const [lead, setLead] = useState<LeadResponse | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const toggleNeed = (need: TaxNeed) => {
    setSelectedNeeds((current) =>
      current.includes(need) ? current.filter((item) => item !== need) : [...current, need],
    );
  };

  const submitLead = async () => {
    setError("");
    if (!/^\\d{5}$/.test(zipCode)) {
      setError("Enter a valid 5-digit ZIP code.");
      return;
    }
    if (selectedNeeds.length === 0) {
      setError("Select at least one tax need.");
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/leads`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          zipCode,
          needs: selectedNeeds,
          timeline,
          firstName: "Demo",
          lastName: "Customer",
          email: "demo@example.com",
          phone: "555-555-5555",
        }),
      });

      if (!response.ok) {
        throw new Error(`API returned ${response.status}`);
      }

      setLead(await response.json());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main>
      <section className="hero">
        <div>
          <p className="eyebrow">Tax Services</p>
          <h1>Find a trusted tax pro without guessing.</h1>
          <p className="hero-copy">
            A scalable lead journey and provider matching system inspired by trusted-provider marketplaces.
          </p>
          <div className="hero-badges">
            <span><ShieldCheck size={16} /> Vetted providers</span>
            <span><Sparkles size={16} /> Match scoring</span>
            <span><Clock size={16} /> Fast routing</span>
          </div>
        </div>
        <div className="system-card">
          <p>System flow</p>
          <strong>Lead intake {"->"} BFF/API {"->"} Matching service {"->"} Provider list {"->"} Analytics</strong>
        </div>
      </section>

      <section className="layout">
        <div className="panel flow-panel">
          <div className="step">
            <span className="step-number">1</span>
            <div>
              <h2>Where are you located?</h2>
              <p>We'll find a tax pro serving your area.</p>
              <label>
                Zip Code
                <input value={zipCode} onChange={(event) => setZipCode(event.target.value)} maxLength={5} />
              </label>
            </div>
          </div>

          <div className="step">
            <span className="step-number">2</span>
            <div>
              <h2>What do you need help with?</h2>
              <p>This gives your pro an idea of how they can serve you.</p>
              <div className="need-grid">
                {needs.map((need) => (
                  <button
                    key={need.value}
                    className={selectedNeeds.includes(need.value) ? "need-card selected" : "need-card"}
                    onClick={() => toggleNeed(need.value)}
                  >
                    <strong>{need.label}</strong>
                    <span>{need.description}</span>
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="step">
            <span className="step-number">3</span>
            <div>
              <h2>How soon do you need help?</h2>
              <div className="timeline-row">
                {timelines.map((item) => (
                  <button
                    key={item.value}
                    className={timeline === item.value ? "chip selected" : "chip"}
                    onClick={() => setTimeline(item.value)}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {error && <p className="error">{error}</p>}
          <button className="primary" onClick={submitLead} disabled={isSubmitting}>
            {isSubmitting ? "Matching..." : "Find my tax pros"} <ArrowRight size={18} />
          </button>
        </div>

        <aside className="panel providers-panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Provider Matching</p>
              <h2>{lead ? `${lead.matches.length} recommended pros` : "Recommended pros appear here"}</h2>
            </div>
            {lead && <span className="status">{lead.status}</span>}
          </div>

          {!lead && (
            <div className="empty-state">
              <MapPin size={28} />
              <p>Submit the flow to create a lead and run the matching service.</p>
            </div>
          )}

          {lead?.matches.map((match) => (
            <article className="provider-card" key={match.provider.id}>
              <div className="rank">#{match.rank}</div>
              <div>
                <div className="provider-title">
                  <h3>{match.provider.name}</h3>
                  <span>{Number(match.provider.rating).toFixed(1)} rating</span>
                </div>
                <p className="firm">{match.provider.firmName} · {match.provider.city}, {match.provider.state}</p>
                <p>{match.provider.bio}</p>
                <div className="provider-meta">
                  <span><BadgeCheck size={14} /> Score {match.score}</span>
                  <span><Clock size={14} /> {match.provider.averageResponseMinutes}m avg response</span>
                </div>
                <p className="reason">{match.scoreReason}</p>
              </div>
            </article>
          ))}
        </aside>
      </section>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
