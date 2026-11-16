export type Project = {
  id: string
  name: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export type ProjectInput = Pick<Project, 'name' | 'description'>

export type ManagedApplication = {
  id: string
  projectId: string
  projectName: string
  name: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export type ApplicationInput = {
  projectId: string
  name: string
  description: string | null
}

export type Feedback = {
  id: string
  applicationId: string
  applicationName: string
  projectId: string
  projectName: string
  rating: number
  comment: string
  submittedAt: string
  createdAt: string
}

export type FeedbackInput = {
  applicationId: string
  rating: number
  comment: string
  submittedAt: string
}

export type DashboardSummary = {
  projects: number
  applications: number
  feedbacks: number
  averageRating: number | null
  medianRating: number | null
  modeRatings: number[]
  minimumRating: number | null
  maximumRating: number | null
  standardDeviation: number | null
  sentimentCounts: Record<Sentiment, number>
  sentimentPercentages: Record<Sentiment, number>
  categoryCounts: Record<string, number>
  feedbacksByMonth: { month: string; count: number }[]
  recurringIssues: { title: string; category: string; feedbackCount: number }[]
  improvementStatuses: Record<string, number>
  latestPriorities: { title: string; score: number; ranking: number }[]
}

export type Category = { id: string; name: string; description: string; system: boolean }
export type Sentiment = 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE'
export type AnalysisClassification = { analysisId: string; feedbackId: string; comment: string; rating: number; sentiment: Sentiment; categoryId: string; category: string; source: 'AUTOMATIC' | 'MANUAL'; originalSentiment: Sentiment | null; originalCategoryId: string | null; originalCategory: string | null; reviewedAt: string | null }
export type IssueGroup = { id: string; title: string; categoryId: string; category: string; feedbackCount: number; feedbackIds: string[] }
export type AnalysisRunSummary = { id: string; projectId: string | null; applicationId: string | null; methodVersion: string; createdAt: string }
export type AnalysisRun = AnalysisRunSummary & { feedbackCount: number; sentimentCounts: Record<Sentiment, number>; categoryCounts: Record<string, number>; classifications: AnalysisClassification[]; groups: IssueGroup[]; methodNotice: string }
export type ImprovementStatus = 'PROPOSED' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'
export type Improvement = { id: string; applicationId: string; applicationName: string; projectName: string; issueGroupId: string | null; title: string; description: string; impact: number; urgency: number; frequency: number; effort: number; assessmentOrigin: string; status: ImprovementStatus }
export type DecisionScore = { improvementId: string; title: string; impact: number; urgency: number; frequency: number; effort: number; normalizedImpact: number; normalizedUrgency: number; normalizedFrequency: number; normalizedEffort: number; finalScore: number; ranking: number }
export type DecisionWeights = { impact: number; urgency: number; frequency: number; effort: number }
export type DecisionRun = { id: string; name: string; weights: DecisionWeights; createdAt: string; ranking: DecisionScore[]; normalization: string; criteriaMeaning: string }
export type DecisionRunSummary = { id: string; name: string; createdAt: string; alternativeCount: number; topScore: number | null }
