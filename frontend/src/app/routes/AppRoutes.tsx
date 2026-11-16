import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { MainLayout } from '../layouts/MainLayout'
import { HomePage } from '../../modules/dashboard/HomePage'
import { ProjectsPage } from '../../modules/projects/ProjectsPage'
import { ApplicationsPage } from '../../modules/applications/ApplicationsPage'
import { FeedbacksPage } from '../../modules/feedbacks/FeedbacksPage'
import { AuthPage } from '../../modules/auth/AuthPage'
import { AnalysesPage } from '../../modules/analyses/AnalysesPage'
import { CategoriesPage } from '../../modules/analyses/CategoriesPage'
import { ImprovementsPage } from '../../modules/improvements/ImprovementsPage'
import { DecisionsPage } from '../../modules/decisions/DecisionsPage'
import { useAuth } from '../providers/useAuth'

function ProtectedLayout() {
  const { user } = useAuth()
  return user ? <MainLayout /> : <Navigate to="/login" replace />
}

export function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<AuthPage />} />
        <Route path="/register" element={<AuthPage register />} />
        <Route element={<ProtectedLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/projects" element={<ProjectsPage />} />
          <Route path="/applications" element={<ApplicationsPage />} />
          <Route path="/feedbacks" element={<FeedbacksPage />} />
          <Route path="/analyses" element={<AnalysesPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/improvements" element={<ImprovementsPage />} />
          <Route path="/decisions" element={<DecisionsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

