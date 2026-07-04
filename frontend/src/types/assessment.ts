// Mirrors the backend AssessmentResponse contract (SPEC.md > "The assessment").
// Keep this in sync with backend/.../dto/AssessmentResponse.java.

export type Rating = "Strong" | "Promising" | "Mixed" | "Weak";

export interface DimensionScore {
  name: string;
  score: number; // 0-100
  reasoning: string;
}

export interface Assessment {
  url: string;
  pageTitle: string;
  overallScore: number; // 0-100
  rating: Rating;
  verdict: string;
  summary: string;
  dimensions: DimensionScore[];
  strengths: string[];
  redFlags: string[];
  diligenceQuestions: string[];
}
