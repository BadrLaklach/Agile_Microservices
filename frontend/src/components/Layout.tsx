import { Outlet, Link, useLocation } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { CrispButton } from "@/components/CrispButton";

export function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  
  const match = location.pathname.match(/\/projects\/([a-f0-9-]+)/);
  const projectId = match ? match[1] : null;
  const showSettingsBtn = projectId && !location.pathname.endsWith("/settings") && (user?.role === "ADMIN" || user?.role === "PO");

  return (
    <div className="min-h-screen bg-brand-straw/10 flex flex-col">
      <header className="bg-white border-b border-brand-burgundy/15 px-4 py-3 flex items-center justify-between shadow-crisp-sm z-10 sticky top-0">
        <div className="flex items-center gap-4">
          <div>
            <h1 className="text-xl font-bold text-brand-burgundy tracking-tight">AGILE WORKSPACE</h1>
            <p className="text-[10px] font-semibold text-brand-burgundy/70 uppercase tracking-widest mt-0.5">Enterprise Edition</p>
          </div>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex flex-col items-end mr-2">
            <span className="text-xs font-bold text-brand-burgundy uppercase">{user?.firstName} {user?.lastName}</span>
            <span className="text-[10px] font-mono text-brand-amber font-bold border border-brand-amber/30 px-1 rounded-sm bg-brand-amber/10">{user?.role}</span>
          </div>
          {showSettingsBtn && (
            <Link to={`/projects/${projectId}/settings`}>
              <CrispButton customVariant="secondary" className="h-7 text-[10px]">Settings</CrispButton>
            </Link>
          )}
          <CrispButton onClick={logout} customVariant="secondary" className="h-7 text-[10px]">Logout</CrispButton>
        </div>
      </header>

      <main className="flex-1 p-4 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
