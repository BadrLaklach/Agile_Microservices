import { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { CrispButton } from "@/components/CrispButton";
import { useAuth } from "@/contexts/AuthContext";
import { InviteMemberModal } from "@/components/Modals";

export function ProjectSettings() {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [project, setProject] = useState<any>(null);
  const [members, setMembers] = useState<any[]>([]);
  const [formData, setFormData] = useState({ name: "", methodology: "" });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [showInviteModal, setShowInviteModal] = useState(false);

  const fetchProjectData = async () => {
    try {
      const [projRes, memRes] = await Promise.all([
        fetch(`/api/v1/projects/${projectId}`),
        fetch(`/api/v1/projects/${projectId}/members`)
      ]);
      if (projRes.ok) {
        const pData = await projRes.json();
        setProject(pData);
        setFormData({ name: pData.name, methodology: pData.methodology });
      }
      if (memRes.ok) {
        setMembers(await memRes.json());
      }
      setLoading(false);
    } catch (e) {
      setError("Failed to load project settings");
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjectData();
  }, [projectId]);

  const handleUpdateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccessMsg("");
    try {
      const res = await fetch(`/api/v1/projects/${projectId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData)
      });
      if (res.ok) {
        setSuccessMsg("Project updated successfully");
        fetchProjectData();
      } else {
        const data = await res.json();
        setError(data.detail || "Update failed");
      }
    } catch (err) {
      setError("Network error");
    }
  };

  const handleRemoveMember = async (memberId: string) => {
    if (!confirm("Remove this member from the project?")) return;
    try {
      const res = await fetch(`/api/v1/projects/${projectId}/members/${memberId}`, { method: "DELETE" });
      if (res.ok || res.status === 204) {
        fetchProjectData();
      } else {
        const data = await res.json();
        alert(data.detail || "Failed to remove member");
      }
    } catch (err) {
      alert("Network error");
    }
  };

  const handleArchiveProject = async () => {
    if (!confirm("Are you sure you want to ARCHIVE this project? Sprints can no longer be created.")) return;
    try {
      const res = await fetch(`/api/v1/projects/${projectId}/archive`, { method: "PATCH" });
      if (res.ok) {
        navigate("/projects");
      } else {
        const data = await res.json();
        alert(data.detail || "Failed to archive project");
      }
    } catch (err) {
      alert("Network error");
    }
  };

  if (loading) return <div className="text-center mt-10 font-bold text-brand-burgundy uppercase tracking-widest text-xs">Loading Settings...</div>;

  if (!user || (user.role !== "ADMIN" && user.role !== "PO")) {
    return (
      <div className="max-w-3xl mx-auto text-center py-10 border-2 border-dashed border-red-200 bg-red-50 mt-10">
        <h2 className="text-red-600 font-bold uppercase tracking-widest mb-4">Access Denied</h2>
        <p className="text-xs font-semibold text-red-500 mb-6">Only Administrators and Product Owners can access Project Settings.</p>
        <Link to={`/projects/${projectId}`}>
          <CrispButton customVariant="secondary">Return to Board</CrispButton>
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto pb-10">
      <div className="flex items-center justify-between border-b border-brand-burgundy/15 pb-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-brand-burgundy uppercase tracking-tight">Project Settings</h2>
          <p className="text-xs font-semibold text-brand-burgundy/70 uppercase tracking-widest mt-1">ID: {projectId}</p>
        </div>
        <Link to={`/projects/${projectId}`}>
          <CrispButton customVariant="secondary" className="px-4 h-9">Back to Board</CrispButton>
        </Link>
      </div>

      {error && <div className="bg-red-50 text-red-600 border border-red-200 p-3 mb-6 text-xs font-bold uppercase rounded-sm">{error}</div>}
      {successMsg && <div className="bg-brand-mint/20 text-brand-burgundy border border-brand-mint p-3 mb-6 text-xs font-bold uppercase rounded-sm">{successMsg}</div>}

      <div className="space-y-8">
        {/* General Settings */}
        <section className="bg-white border border-brand-burgundy/15 p-5 rounded-sm shadow-crisp-sm">
          <h3 className="font-bold text-brand-burgundy uppercase tracking-widest text-xs border-b border-brand-burgundy/10 pb-2 mb-4">General Details</h3>
          <form onSubmit={handleUpdateProject} className="space-y-4">
            <div>
              <label className="block text-[10px] font-bold text-brand-burgundy/70 uppercase mb-1">Project Name</label>
              <input 
                type="text" 
                value={formData.name} 
                onChange={e => setFormData({ ...formData, name: e.target.value })} 
                className="w-full flex h-9 rounded-sm border border-brand-burgundy/20 bg-transparent px-3 py-1 text-sm shadow-crisp-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy"
                required
              />
            </div>
            <div>
              <label className="block text-[10px] font-bold text-brand-burgundy/70 uppercase mb-1">Methodology</label>
              <select 
                value={formData.methodology} 
                onChange={e => setFormData({ ...formData, methodology: e.target.value })} 
                className="w-full flex h-9 rounded-sm border border-brand-burgundy/20 bg-transparent px-3 py-1 text-sm shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy"
              >
                <option value="SCRUM">SCRUM</option>
                <option value="KANBAN">KANBAN</option>
                <option value="HYBRID">HYBRID</option>
              </select>
            </div>
            <div className="pt-2">
              <CrispButton customVariant="action" type="submit" className="px-6 h-9">Save Changes</CrispButton>
            </div>
          </form>
        </section>

        {/* Member Management */}
        <section className="bg-white border border-brand-burgundy/15 p-5 rounded-sm shadow-crisp-sm">
          <div className="flex justify-between items-center border-b border-brand-burgundy/10 pb-2 mb-4">
            <h3 className="font-bold text-brand-burgundy uppercase tracking-widest text-xs">Team Members</h3>
            <CrispButton customVariant="secondary" className="h-7 text-[10px] px-3" onClick={() => setShowInviteModal(true)}>+ Invite Member</CrispButton>
          </div>
          <div className="space-y-2">
            {members.map(m => (
              <div key={m.userId} className="flex justify-between items-center p-3 border border-brand-burgundy/10 rounded-sm bg-brand-straw/10">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-xs text-brand-burgundy">{m.firstName} {m.lastName}</span>
                    {m.userId === project?.createdBy && <span className="bg-brand-burgundy text-white text-[9px] px-1 rounded-sm uppercase font-bold tracking-wider">Creator</span>}
                  </div>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-[10px] font-mono text-brand-burgundy/60">{m.email}</span>
                    <span className="bg-brand-mint/40 border border-brand-mint text-brand-burgundy text-[9px] px-1 rounded-sm font-bold uppercase">{m.role}</span>
                  </div>
                </div>
                {m.userId !== project?.createdBy && (
                  <button onClick={() => handleRemoveMember(m.userId)} className="text-[10px] uppercase font-bold text-red-500 hover:text-red-700 transition-colors">Remove</button>
                )}
              </div>
            ))}
          </div>
        </section>

        {/* Danger Zone */}
        <section className="border-2 border-dashed border-red-200 bg-red-50 p-5 rounded-sm">
          <h3 className="font-bold text-red-600 uppercase tracking-widest text-xs border-b border-red-200 pb-2 mb-4">Danger Zone</h3>
          <div className="flex justify-between items-center">
            <p className="text-xs font-semibold text-red-500">Archiving this project will prevent any new sprints from being planned.</p>
            <CrispButton onClick={handleArchiveProject} className="bg-red-600 border-red-700 text-white hover:bg-red-700 active:translate-y-[2px] active:shadow-none shadow-[2px_2px_0px_0px_rgb(153,27,27)] h-9 px-6 rounded-sm uppercase tracking-widest text-[10px] font-extrabold transition-all">Archive Project</CrispButton>
          </div>
        </section>
      </div>

      {showInviteModal && <InviteMemberModal projectId={projectId!} onClose={() => { setShowInviteModal(false); fetchProjectData(); }} />}
    </div>
  );
}
