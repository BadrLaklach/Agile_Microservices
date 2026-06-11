import { useState, useEffect } from "react";
import { CrispButton } from "./CrispButton";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, ReferenceLine, LineChart, Line } from 'recharts';

export function InviteMemberModal({ projectId, onClose }: { projectId: string, onClose: () => void }) {
  const [formData, setFormData] = useState({ email: "", role: "DEV", isNew: false, firstName: "", lastName: "", password: "" });
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await fetch(`/api/v1/projects/${projectId}/members`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData)
      });
      if (res.ok) onClose();
      else {
        const data = await res.json();
        setError(data.detail || "Failed to invite member");
      }
    } catch (err) {
      setError("Network error");
    }
  };

  return (
    <div className="fixed inset-0 bg-brand-burgundy/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white border border-brand-burgundy/15 p-4 rounded-sm shadow-crisp-md w-full max-w-md">
        <h3 className="text-lg font-bold text-brand-burgundy uppercase mb-3 border-b border-brand-burgundy/15 pb-2">Invite Member</h3>
        {error && <div className="mb-3 p-2 bg-red-50 text-red-700 text-xs font-semibold border border-brand-amber/30 rounded-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Email</Label>
            <Input required type="email" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Role</Label>
            <select value={formData.role} onChange={e => setFormData({...formData, role: e.target.value})} className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
              <option value="PO">Product Owner</option>
              <option value="SM">Scrum Master</option>
              <option value="DEV">Developer</option>
            </select>
          </div>
          <div className="flex items-center gap-2 mt-2">
            <input type="checkbox" id="isNew" checked={formData.isNew} onChange={e => setFormData({...formData, isNew: e.target.checked})} />
            <Label htmlFor="isNew" className="text-[10px] font-bold uppercase text-brand-burgundy">Is this a new user?</Label>
          </div>
          {formData.isNew && (
            <div className="grid grid-cols-2 gap-2 p-2 bg-brand-straw/20 border border-brand-burgundy/10 rounded-sm">
              <div className="space-y-1">
                <Label className="text-[10px] font-bold uppercase text-brand-burgundy">First Name</Label>
                <Input required value={formData.firstName} onChange={e => setFormData({...formData, firstName: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
              </div>
              <div className="space-y-1">
                <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Last Name</Label>
                <Input required value={formData.lastName} onChange={e => setFormData({...formData, lastName: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
              </div>
              <div className="space-y-1 col-span-2">
                <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Temporary Password</Label>
                <Input required type="password" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
              </div>
            </div>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <CrispButton type="button" onClick={onClose} customVariant="secondary">Cancel</CrispButton>
            <CrispButton type="submit" customVariant="action">Invite</CrispButton>
          </div>
        </form>
      </div>
    </div>
  );
}

export function CreateSprintModal({ projectId, onClose, onCreated }: { projectId: string, onClose: () => void, onCreated: () => void }) {
  const [formData, setFormData] = useState({ name: "", goal: "", capacity: 40, startDate: "", endDate: "" });
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const start = new Date(formData.startDate);
    const end = new Date(formData.endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (start < today) {
      setError("Start date cannot be in the past");
      return;
    }
    if (end < start) {
      setError("End date must be after start date");
      return;
    }

    try {
      const res = await fetch(`/api/v1/projects/${projectId}/sprints`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData)
      });
      if (res.ok) {
        onCreated();
        onClose();
      } else {
        const data = await res.json();
        setError(data.detail || "Failed to create sprint");
      }
    } catch (err) {
      setError("Network error");
    }
  };

  return (
    <div className="fixed inset-0 bg-brand-burgundy/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white border border-brand-burgundy/15 p-4 rounded-sm shadow-crisp-md w-full max-w-md">
        <h3 className="text-lg font-bold text-brand-burgundy uppercase mb-3 border-b border-brand-burgundy/15 pb-2">Create Sprint</h3>
        {error && <div className="mb-3 p-2 bg-red-50 text-red-700 text-xs font-semibold border border-brand-amber/30 rounded-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Sprint Name</Label>
            <Input required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Goal</Label>
            <Input value={formData.goal} onChange={e => setFormData({...formData, goal: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
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
          <div className="flex justify-end gap-2 pt-2">
            <CrispButton type="button" onClick={onClose} customVariant="secondary">Cancel</CrispButton>
            <CrispButton type="submit" customVariant="action">Create</CrispButton>
          </div>
        </form>
      </div>
    </div>
  );
}

export function CreateTaskModal({ projectId, sprintId, onClose, onCreated }: { projectId: string, sprintId?: string | null, onClose: () => void, onCreated: () => void }) {
  const [formData, setFormData] = useState({ title: "", description: "", type: "USER_STORY", priority: "MEDIUM", estimate: 5 });
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await fetch(`/api/v1/tasks`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...formData, projectId, sprintId: sprintId || undefined })
      });
      if (res.ok) {
        onCreated();
        onClose();
      } else {
        const data = await res.json();
        setError(data.detail || "Failed to create task");
      }
    } catch (err) {
      setError("Network error");
    }
  };

  return (
    <div className="fixed inset-0 bg-brand-burgundy/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white border border-brand-burgundy/15 p-4 rounded-sm shadow-crisp-md w-full max-w-md">
        <h3 className="text-lg font-bold text-brand-burgundy uppercase mb-3 border-b border-brand-burgundy/15 pb-2">Create Task</h3>
        {error && <div className="mb-3 p-2 bg-red-50 text-red-700 text-xs font-semibold border border-brand-amber/30 rounded-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Title</Label>
            <Input required value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Type</Label>
              <select value={formData.type} onChange={e => setFormData({...formData, type: e.target.value})} className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
                <option value="USER_STORY">USER STORY</option>
                <option value="BUG">BUG</option>
                <option value="TECHNICAL_TASK">TECHNICAL TASK</option>
              </select>
            </div>
            <div className="space-y-1">
              <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Priority</Label>
              <select value={formData.priority} onChange={e => setFormData({...formData, priority: e.target.value})} className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
                <option value="CRITICAL">CRITICAL</option>
                <option value="HIGH">HIGH</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="LOW">LOW</option>
              </select>
            </div>
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Estimate (Points)</Label>
            <Input type="number" required value={formData.estimate} onChange={e => setFormData({...formData, estimate: parseInt(e.target.value)})} className="h-8 rounded-sm text-xs shadow-crisp-sm border-brand-burgundy/20" />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <CrispButton type="button" onClick={onClose} customVariant="secondary">Cancel</CrispButton>
            <CrispButton type="submit" customVariant="action">Create Task</CrispButton>
          </div>
        </form>
      </div>
    </div>
  );
}

export function AssignBacklogTaskModal({ task, projectId, sprintId, onClose, onAssigned }: { task: any, projectId: string, sprintId: string, onClose: () => void, onAssigned: () => void }) {
  const [assigneeId, setAssigneeId] = useState("");
  const [status, setStatus] = useState("TODO");
  const [members, setMembers] = useState<any[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetch(`/api/v1/projects/${projectId}/members`)
      .then(res => res.ok ? res.json() : [])
      .then(data => setMembers(Array.isArray(data) ? data : data.content || []))
      .catch(() => {});
  }, [projectId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // 1. Assign to Sprint
      let res = await fetch(`/api/v1/projects/${projectId}/sprints/${sprintId}/tasks`, {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ taskId: task.id || task.taskId })
      });
      if (!res.ok) {
        const d = await res.json();
        throw new Error(d.detail || "Failed to assign to sprint");
      }

      // 2. Update Assignee via PUT
      if (assigneeId) {
        res = await fetch(`/api/v1/tasks/${task.id || task.taskId}`, {
          method: "PUT", headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            title: task.title, description: task.description, type: task.type,
            priority: task.priority, estimate: task.estimate,
            projectId: task.projectId || projectId, assigneeId: assigneeId
          })
        });
        if (!res.ok) {
          const d = await res.json();
          throw new Error(d.detail || "Failed to update assignee");
        }
      }

      // 3. Update Status
      if (status !== "TODO") {
        res = await fetch(`/api/v1/tasks/${task.id || task.taskId}/status`, {
          method: "PATCH", headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ status })
        });
        if (!res.ok) {
          const d = await res.json();
          throw new Error(d.detail || "Failed to update status");
        }
      }

      onAssigned();
    } catch (err: any) {
      setError(err.message || "Network error");
    }
  };

  return (
    <div className="fixed inset-0 bg-brand-burgundy/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white border border-brand-burgundy/15 p-4 rounded-sm shadow-crisp-md w-full max-w-sm">
        <h3 className="text-lg font-bold text-brand-burgundy uppercase mb-3 border-b border-brand-burgundy/15 pb-2">Assign Task to Sprint</h3>
        {error && <div className="mb-3 p-2 bg-red-50 text-red-700 text-xs font-semibold border border-brand-amber/30 rounded-sm">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy/70">Task</Label>
            <p className="text-xs font-bold text-brand-burgundy">{task.title}</p>
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Assignee</Label>
            <select value={assigneeId} onChange={e => setAssigneeId(e.target.value)} className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
              <option value="">Unassigned</option>
              {Array.isArray(members) && members.filter(m => m.role !== 'MA').map(m => (
                <option key={m.userId} value={m.userId}>{m.firstName} {m.lastName} ({m.role})</option>
              ))}
            </select>
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy">Initial Status</Label>
            <select value={status} onChange={e => setStatus(e.target.value)} className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">IN PROGRESS</option>
              <option value="DONE">DONE</option>
            </select>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <CrispButton type="button" onClick={onClose} customVariant="secondary">Cancel</CrispButton>
            <CrispButton type="submit" customVariant="action">Confirm</CrispButton>
          </div>
        </form>
      </div>
    </div>
  );
}

import { Badge } from "./ui/badge";

export function TaskPanel({ task, projectId, sprints, onClose, onUpdated }: { task: any, projectId: string, sprints?: any[], onClose: () => void, onUpdated: () => void }) {
  const [status, setStatus] = useState(task.status);
  const [assigneeId, setAssigneeId] = useState(task.assigneeId || "");
  const [selectedSprintForTask, setSelectedSprintForTask] = useState("");
  const [members, setMembers] = useState<any[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetch(`/api/v1/projects/${projectId}/members`)
      .then(res => res.ok ? res.json() : [])
      .then(data => setMembers(Array.isArray(data) ? data : data.content || []))
      .catch(() => {});
  }, [projectId]);

  const handleUpdateStatus = async (newStatus: string) => {
    try {
      const res = await fetch(`/api/v1/tasks/${task.id || task.taskId}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: newStatus })
      });
      if (res.ok) {
        setStatus(newStatus);
        onUpdated();
      } else {
        const data = await res.json();
        setError(data.detail || "Failed to update status");
      }
    } catch (err) {
      setError("Network error");
    }
  };

  const handleUpdateAssignee = async (newAssigneeId: string) => {
    try {
      // PUT requires full task body
      const res = await fetch(`/api/v1/tasks/${task.id || task.taskId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: task.title,
          description: task.description,
          type: task.type,
          priority: task.priority,
          estimate: task.estimate,
          projectId: task.projectId || projectId,
          assigneeId: newAssigneeId || null
        })
      });
      if (res.ok) {
        setAssigneeId(newAssigneeId);
        onUpdated();
      } else {
        const data = await res.json();
        setError(data.detail || "Failed to update assignee");
      }
    } catch (err) {
      setError("Network error");
    }
  };

  const handleAddToSprint = async () => {
    if (!selectedSprintForTask) {
      setError("Please select a sprint first");
      return;
    }
    try {
      const res = await fetch(`/api/v1/projects/${projectId}/sprints/${selectedSprintForTask}/tasks`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ taskId: task.id || task.taskId })
      });
      if (res.ok) {
        onUpdated();
        onClose();
      } else {
        const data = await res.json();
        setError(data.detail || "Failed to add to sprint");
      }
    } catch (err) {
      setError("Network error");
    }
  };

  const handleDeleteTask = async () => {
    if (!confirm("Are you sure you want to delete this task?")) return;
    try {
      const res = await fetch(`/api/v1/tasks/${task.id || task.taskId}`, { method: 'DELETE' });
      if (res.ok || res.status === 204) {
        onUpdated();
        onClose();
      } else {
        setError("Failed to delete task");
      }
    } catch (e) {
      setError("Network error");
    }
  };

  return (
    <div className="fixed inset-y-0 right-0 w-96 bg-white border-l border-brand-burgundy/20 shadow-[-4px_0_15px_-3px_rgba(79,37,46,0.1)] z-40 p-4 overflow-y-auto">
      <div className="flex justify-between items-start border-b border-brand-burgundy/15 pb-3 mb-4">
        <div>
          <h2 className="text-sm font-bold text-brand-burgundy uppercase tracking-tight">{task.title}</h2>
          <div className="flex items-center gap-2 mt-2">
            <span className="text-[10px] font-mono text-brand-amber bg-brand-amber/10 px-1 rounded-sm border border-brand-amber/30 font-bold">{task.id || task.taskId}</span>
            <Badge variant="outline" className={`text-[9px] px-1.5 py-0 rounded-sm uppercase ${task.type === 'BUG' ? 'border-red-500 text-red-600 bg-red-50' : task.type === 'USER_STORY' ? 'border-brand-mint text-brand-burgundy bg-brand-mint/20' : 'border-slate-400 text-slate-600 bg-slate-50'}`}>{task.type || task.epicName || 'TASK'}</Badge>
          </div>
        </div>
        <button onClick={onClose} className="text-brand-burgundy/50 hover:text-brand-burgundy font-bold text-xl leading-none">&times;</button>
      </div>
      
      {error && <div className="mb-3 p-2 bg-red-50 text-red-700 text-xs font-semibold border border-brand-amber/30 rounded-sm">{error}</div>}

      <div className="space-y-4">
        <div>
          <Label className="text-[10px] font-bold uppercase text-brand-burgundy/70">Status</Label>
          <select value={status} onChange={e => handleUpdateStatus(e.target.value)} className="flex h-8 w-full mt-1 rounded-sm border border-brand-burgundy/20 bg-brand-straw/10 px-2 py-1 text-xs font-bold text-brand-burgundy shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
            <option value="TODO">TODO</option>
            <option value="IN_PROGRESS">IN PROGRESS</option>
            <option value="DONE">DONE</option>
          </select>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy/70">Type</Label>
            <p className="text-xs font-semibold text-brand-burgundy">{task.type}</p>
          </div>
          <div>
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy/70">Priority</Label>
            <p className="text-xs font-semibold text-brand-burgundy">{task.priority}</p>
          </div>
          <div>
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy/70">Estimate</Label>
            <div className="w-6 h-6 flex items-center justify-center bg-brand-straw border border-brand-burgundy/20 rounded-full text-xs font-mono font-bold text-brand-burgundy mt-1">
              {task.estimate}
            </div>
          </div>
        </div>

        <div className="pt-2 border-t border-brand-burgundy/10">
          <Label className="text-[10px] font-bold uppercase text-brand-burgundy/70">Assignee</Label>
          <select value={assigneeId} onChange={e => handleUpdateAssignee(e.target.value)} className="flex h-8 w-full mt-1 rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
            <option value="">Unassigned</option>
            {Array.isArray(members) && members.filter(m => m.role !== 'MA').map(m => (
              <option key={m.userId} value={m.userId}>{m.firstName} {m.lastName} ({m.role})</option>
            ))}
          </select>
        </div>

        {!task.sprintId && sprints && sprints.filter(s => s.status !== 'COMPLETED').length > 0 && (
          <div className="pt-4 border-t border-brand-burgundy/10">
            <Label className="text-[10px] font-bold uppercase text-brand-burgundy/70">Assign to Sprint</Label>
            <div className="flex gap-2 mt-1">
              <select value={selectedSprintForTask} onChange={e => setSelectedSprintForTask(e.target.value)} className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy">
                <option value="" disabled>Select Sprint</option>
                {sprints.filter(s => s.status !== 'COMPLETED').map(s => (
                  <option key={s.id} value={s.id}>{s.name} ({s.status})</option>
                ))}
              </select>
              <CrispButton onClick={handleAddToSprint} customVariant="action" className="text-xs h-8 px-4">Push</CrispButton>
            </div>
          </div>
        )}

        <div className="pt-4 mt-4 border-t border-red-500/20">
          <button onClick={handleDeleteTask} className="text-[10px] font-bold uppercase text-red-500 hover:text-red-700 transition-colors">
            Delete Task
          </button>
        </div>
      </div>
    </div>
  );
}

export function MetricsModal({ projectId, sprintId, onClose }: { projectId: string, sprintId?: string | null, onClose: () => void }) {
  const [burndown, setBurndown] = useState<any>(null);
  const [velocity, setVelocity] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const [vRes, bRes] = await Promise.all([
          fetch(`/api/v1/projects/${projectId}/metrics/velocity`),
          sprintId ? fetch(`/api/v1/projects/${projectId}/sprints/${sprintId}/metrics/burndown`) : Promise.resolve(null)
        ]);
        if (vRes.ok) setVelocity(await vRes.json());
        if (bRes && bRes.ok) setBurndown(await bRes.json());
      } catch (e) {}
      setLoading(false);
    };
    fetchMetrics();
  }, [projectId, sprintId]);

  const handleDownload = (data: any, filename: string) => {
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="fixed inset-0 bg-brand-burgundy/20 z-50 flex items-center justify-center p-4">
      <div className="bg-white border border-brand-burgundy/20 rounded-sm shadow-crisp-lg w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh]">
        <div className="bg-brand-mint/30 border-b border-brand-burgundy/15 p-4 flex justify-between items-center">
          <div>
            <h2 className="text-sm font-bold text-brand-burgundy uppercase tracking-widest">Project Analytics & Metrics</h2>
            <p className="text-[10px] text-brand-burgundy/60 font-semibold uppercase mt-1">Velocity & Burndown Data</p>
          </div>
          <button onClick={onClose} className="text-brand-burgundy/50 hover:text-brand-burgundy font-bold text-xl leading-none">&times;</button>
        </div>
        <div className="p-6 overflow-y-auto space-y-6">
          {loading ? (
            <p className="text-xs text-center uppercase tracking-widest font-bold text-brand-burgundy/50 py-10">Loading Metrics...</p>
          ) : (
            <>
              {/* Velocity Report */}
              <div className="border border-brand-burgundy/15 rounded-sm overflow-hidden">
                <div className="bg-brand-straw/30 p-3 border-b border-brand-burgundy/15 flex justify-between items-center">
                  <h3 className="text-xs font-bold text-brand-burgundy uppercase tracking-widest">Velocity Report</h3>
                  {velocity && <CrispButton onClick={() => handleDownload(velocity, `velocity_report_${projectId}.json`)} customVariant="secondary" className="h-7 px-3 text-[10px]">Download JSON</CrispButton>}
                </div>
                <div className="p-4">
                  {velocity ? (
                    <div>
                      <div className="flex gap-4 mb-4">
                        <div className="bg-brand-burgundy text-white p-3 rounded-sm flex-1 text-center">
                          <p className="text-[10px] uppercase font-bold text-white/70 mb-1">Avg Velocity</p>
                          <p className="text-2xl font-mono font-bold">{velocity.averageVelocity}</p>
                        </div>
                      </div>
                      
                      {velocity.sprints && velocity.sprints.length > 0 ? (
                        <div className="h-64 w-full mt-4">
                          <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={velocity.sprints} margin={{ top: 20, right: 30, left: 0, bottom: 20 }}>
                              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                              <XAxis dataKey="sprintName" tick={{ fontSize: 10, fill: '#4F252E' }} axisLine={false} tickLine={false} dy={10} />
                              <YAxis tick={{ fontSize: 10, fill: '#4F252E' }} axisLine={false} tickLine={false} dx={-10} />
                              <RechartsTooltip 
                                cursor={{ fill: 'rgba(79, 37, 46, 0.05)' }}
                                contentStyle={{ backgroundColor: '#fff', border: '1px solid rgba(79, 37, 46, 0.2)', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold', color: '#4F252E' }}
                                itemStyle={{ color: '#4F252E' }}
                              />
                              <ReferenceLine y={velocity.averageVelocity} stroke="#059669" strokeDasharray="3 3" label={{ position: 'top', value: 'AVG', fill: '#059669', fontSize: 10, fontWeight: 'bold' }} />
                              <Bar dataKey="velocity" fill="#4F252E" radius={[4, 4, 0, 0]} maxBarSize={50} />
                            </BarChart>
                          </ResponsiveContainer>
                        </div>
                      ) : (
                        <p className="text-[10px] text-brand-burgundy/50 uppercase font-bold italic mt-4 text-center">No completed sprints yet</p>
                      )}
                    </div>
                  ) : <p className="text-xs text-red-500">Failed to load velocity report</p>}
                </div>
              </div>

              {/* Burndown Chart */}
              <div className="border border-brand-burgundy/15 rounded-sm overflow-hidden">
                <div className="bg-brand-mint/20 p-3 border-b border-brand-burgundy/15 flex justify-between items-center">
                  <h3 className="text-xs font-bold text-brand-burgundy uppercase tracking-widest">Active Sprint Burndown</h3>
                  {burndown && <CrispButton onClick={() => handleDownload(burndown, `burndown_sprint_${sprintId}.json`)} customVariant="secondary" className="h-7 px-3 text-[10px]">Download JSON</CrispButton>}
                </div>
                <div className="p-4">
                  {burndown ? (
                    <div>
                      <p className="text-xs font-bold text-brand-burgundy mb-4">{burndown.sprintName}</p>
                      <div className="flex gap-2 mb-4">
                        <div className="bg-slate-50 border border-slate-200 p-2 rounded-sm flex-1 text-center">
                          <p className="text-[9px] uppercase font-bold text-slate-500">Total Estimate</p>
                          <p className="text-lg font-mono font-bold text-slate-700">{burndown.totalEstimate}</p>
                        </div>
                        <div className="bg-brand-mint/10 border border-brand-mint p-2 rounded-sm flex-1 text-center">
                          <p className="text-[9px] uppercase font-bold text-brand-mint">Completed</p>
                          <p className="text-lg font-mono font-bold text-brand-burgundy">{burndown.completedEstimate}</p>
                        </div>
                        <div className="bg-brand-amber/10 border border-brand-amber p-2 rounded-sm flex-1 text-center">
                          <p className="text-[9px] uppercase font-bold text-brand-amber">Remaining</p>
                          <p className="text-lg font-mono font-bold text-brand-burgundy">{burndown.remainingEstimate}</p>
                        </div>
                      </div>

                      {burndown.idealBurndown && burndown.idealBurndown.length > 0 ? (
                        <div className="h-64 w-full mt-6">
                          <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={burndown.idealBurndown.map((d: any) => ({ ...d, date: d.date.slice(5) }))} margin={{ top: 10, right: 20, left: 0, bottom: 20 }}>
                              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                              <XAxis dataKey="date" tick={{ fontSize: 10, fill: '#4F252E' }} axisLine={false} tickLine={false} dy={10} />
                              <YAxis tick={{ fontSize: 10, fill: '#4F252E' }} axisLine={false} tickLine={false} dx={-10} domain={[0, 'dataMax']} />
                              <RechartsTooltip 
                                contentStyle={{ backgroundColor: '#fff', border: '1px solid rgba(79, 37, 46, 0.2)', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold', color: '#4F252E' }}
                                itemStyle={{ color: '#4F252E' }}
                              />
                              <Line type="monotone" dataKey="ideal" name="Ideal Burndown" stroke="#4F252E" strokeWidth={3} strokeDasharray="5 5" dot={{ r: 4, fill: '#4F252E' }} activeDot={{ r: 6 }} />
                            </LineChart>
                          </ResponsiveContainer>
                        </div>
                      ) : (
                        <p className="text-[10px] text-brand-burgundy/50 uppercase font-bold italic mt-4 text-center">Not enough data points for burndown chart</p>
                      )}
                    </div>
                  ) : (
                    <p className="text-xs text-brand-burgundy/50 font-bold uppercase italic">{sprintId ? 'Failed to load burndown' : 'No active sprint to analyze'}</p>
                  )}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
