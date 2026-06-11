import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";

export const ProtectedRoute = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen bg-brand-straw/10 flex items-center justify-center text-xs font-bold text-brand-burgundy uppercase tracking-widest">Loading Session...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};
