import { useState, useEffect } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { CrispButton } from "@/components/CrispButton";
import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface Project {
  id: string;
  name: string;
  description: string;
  methodology: string;
  status: string;
  startDate: string;
  endDate: string;
}

export function Projects() {
  const { user } = useAuth();
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [createError, setCreateError] = useState("");
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    methodology: "SCRUM",
    startDate: "",
    endDate: ""
  });

  const fetchProjects = () => {
    fetch("/api/v1/projects")
      .then(res => res.ok ? res.json() : [])
      .then(data => {
        setProjects(Array.isArray(data) ? data : []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError("");
    try {
      const res = await fetch("/api/v1/projects", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });
      if (res.ok) {
        setIsModalOpen(false);
        setFormData({ name: "", description: "", methodology: "SCRUM", startDate: "", endDate: "" });
        fetchProjects(); // refresh list
      } else {
        const data = await res.json();
        setCreateError(data.detail || "Failed to create project");
      }
    } catch (err) {
      setCreateError("Network error occurred");
    }
  };

  const canCreateProject = user?.role === "ADMIN" || user?.role === "PO";

  return (
    <div className="space-y-4 max-w-5xl mx-auto">
      <div className="flex items-center justify-between border-b border-brand-burgundy/15 pb-2">
        <h2 className="text-lg font-bold text-brand-burgundy uppercase tracking-tight">Project Portfolios</h2>
        {canCreateProject && (
          <CrispButton customVariant="action" onClick={() => setIsModalOpen(true)}>New Project</CrispButton>
        )}
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-brand-burgundy/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white border border-brand-burgundy/15 p-4 rounded-sm shadow-crisp-md w-full max-w-md">
            <h3 className="text-lg font-bold text-brand-burgundy uppercase mb-3 border-b border-brand-burgundy/15 pb-2">Create New Project</h3>
            {createError && <div className="mb-3 p-2 bg-red-50 text-red-700 text-xs font-semibold border border-brand-amber/30 rounded-sm">{createError}</div>}
            <form onSubmit={handleCreateProject} className="space-y-3">
              <div className="space-y-1">
                <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Project Name</Label>
                <Input required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
              </div>
              <div className="space-y-1">
                <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Description</Label>
                <Input value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Start Date</Label>
                  <Input type="date" required value={formData.startDate} onChange={e => setFormData({...formData, startDate: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
                </div>
                <div className="space-y-1">
                  <Label className="text-[10px] font-bold uppercase text-brand-burgundy">End Date</Label>
                  <Input type="date" required value={formData.endDate} onChange={e => setFormData({...formData, endDate: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
                </div>
              </div>
              <div className="space-y-1">
                <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Methodology</Label>
                <select value={formData.methodology} onChange={e => setFormData({...formData, methodology: e.target.value})} className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
                  <option value="SCRUM">SCRUM</option>
                  <option value="KANBAN">KANBAN</option>
                  <option value="HYBRID">HYBRID</option>
                </select>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <CrispButton type="button" onClick={() => setIsModalOpen(false)} customVariant="secondary">Cancel</CrispButton>
                <CrispButton type="submit" customVariant="action">Create</CrispButton>
              </div>
            </form>
          </div>
        </div>
      )}

      {loading ? (
        <div className="text-xs font-bold text-brand-burgundy uppercase">Loading projects...</div>
      ) : projects.length === 0 ? (
        <div className="p-4 bg-white border border-brand-burgundy/15 rounded-sm shadow-crisp-sm text-xs font-medium text-brand-burgundy/70">
          No projects found. You are not a member of any active projects.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map(p => (
            <Link key={p.id} to={`/projects/${p.id}`}>
              <Card className="p-4 bg-white border border-brand-burgundy/15 rounded-sm shadow-crisp-sm hover:shadow-crisp-md hover:border-brand-burgundy/30 transition-all cursor-pointer">
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-sm font-bold text-brand-burgundy uppercase leading-snug">{p.name}</h3>
                  <span className="text-[9px] font-mono font-bold bg-brand-mint/40 text-brand-burgundy px-1 py-0.5 rounded-sm border border-brand-mint">{p.methodology}</span>
                </div>
                <p className="text-[11px] text-brand-burgundy/70 line-clamp-2 mb-3">{p.description}</p>
                <div className="flex items-center justify-between text-[10px] font-bold text-brand-burgundy border-t border-dashed border-brand-burgundy/10 pt-2">
                  <span>{p.startDate} - {p.endDate}</span>
                  <span className={`px-1.5 py-0.5 rounded-sm border uppercase ${p.status === 'ACTIVE' ? 'bg-green-50 text-green-700 border-green-200' : 'bg-slate-50 text-slate-500 border-slate-200'}`}>
                    {p.status}
                  </span>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
