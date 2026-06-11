import { useState, useEffect } from 'react'
import { CrispButton } from '@/components/CrispButton'
import { SprintColumn, AgileTask } from '@/components/SprintColumn'
import { Badge } from '@/components/ui/badge'
import { useParams, Link } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { InviteMemberModal, CreateSprintModal, CreateTaskModal, TaskPanel, AssignBacklogTaskModal, MetricsModal } from '@/components/Modals'

export function ProjectBoard() {
  const { projectId } = useParams<{ projectId: string }>()
  const { user } = useAuth()
  
  const [sprints, setSprints] = useState<any[]>([])
  const [sprintTasksMap, setSprintTasksMap] = useState<Record<string, any[]>>({})
  const [backlogTasks, setBacklogTasks] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  
  const [view, setView] = useState<"BOARD" | "BACKLOG">("BACKLOG")

  // Modals state
  const [showInvite, setShowInvite] = useState(false)
  const [showSprintModal, setShowSprintModal] = useState(false)
  const [showTaskModal, setShowTaskModal] = useState(false)
  const [showMetricsModal, setShowMetricsModal] = useState(false)
  const [createTaskSprintId, setCreateTaskSprintId] = useState<string | null>(null)
  const [selectedTask, setSelectedTask] = useState<any | null>(null)
  const [taskToAssign, setTaskToAssign] = useState<any | null>(null)

  const fetchSprints = () => {
    fetch(`/api/v1/projects/${projectId}/sprints`)
      .then(res => res.ok ? res.json() : [])
      .then(sprintsData => {
        setSprints(sprintsData);
        sprintsData.forEach((s: any) => {
          fetch(`/api/v1/projects/${projectId}/sprints/${s.id}/tasks`)
            .then(res => res.ok ? res.json() : [])
            .then(tasks => setSprintTasksMap(prev => ({ ...prev, [s.id]: tasks })))
        });
      })
  }

  const fetchBacklog = () => {
    setLoading(true)
    fetch(`/api/v1/tasks?projectId=${projectId}`)
      .then(res => res.ok ? res.json() : { content: [] })
      .then(data => {
        const content = data.content || data || [];
        setBacklogTasks(content.filter((t: any) => !t.sprintId));
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }

  useEffect(() => {
    fetchSprints()
    fetchBacklog()
  }, [projectId])

  const refreshData = () => {
    fetchSprints()
    fetchBacklog()
  }

  const handleStartSprint = async (sprintId: string) => {
    try {
      await fetch(`/api/v1/projects/${projectId}/sprints/${sprintId}/start`, { method: 'PATCH' })
      fetchSprints()
      setView("BOARD")
    } catch (err) {}
  }

  const handleCloseSprint = async (sprintId: string) => {
    try {
      await fetch(`/api/v1/projects/${projectId}/sprints/${sprintId}/close`, { method: 'PATCH' })
      fetchSprints()
      setView("BACKLOG")
    } catch (err) {}
  }

  const handleDeleteSprint = async (sprintId: string) => {
    if (!confirm("Are you sure you want to delete this sprint?")) return;
    try {
      await fetch(`/api/v1/projects/${projectId}/sprints/${sprintId}`, { method: 'DELETE' })
      refreshData()
    } catch (err) {}
  }

  const canCreateSprint = user?.role === "ADMIN" || user?.role === "PO" || user?.role === "SM"
  const canStartSprint = user?.role === "ADMIN" || user?.role === "PO" || user?.role === "SM"
  const canCreateTask = user?.role === "ADMIN" || user?.role === "PO" || user?.role === "MA" || user?.role === "SM"
  const canInvite = user?.role === "ADMIN" || user?.role === "PO"

  const activeSprint = sprints.find(s => s.status === 'ACTIVE')

  // Map to UI format
  const mapTask = (t: any) => ({
    id: t.id || t.taskId,
    title: t.title,
    epicName: t.type,
    storyPoints: t.estimate || 0,
    priority: t.priority || "MEDIUM",
    status: t.status || "TODO",
    assigneeId: t.assigneeId,
    raw: t // Keep original to pass to panel
  })

  const sprintTasks = sprintTasksMap[activeSprint?.id] || []
  const mappedTasks = sprintTasks.map(mapTask)
  const todoTasks = mappedTasks.filter(t => t.status === "TODO")
  const inProgressTasks = mappedTasks.filter(t => t.status === "IN_PROGRESS")
  const doneTasks = mappedTasks.filter(t => t.status === "COMPLETED" || t.status === "DONE")

  const BacklogTaskRow = ({ task }: { task: any }) => {
    const isAssignedToOther = task.assigneeId && task.assigneeId !== user?.id && task.assigneeId !== user?.userId;
    return (
      <div 
        className={`flex justify-between items-center p-3 border border-brand-burgundy/15 rounded-sm transition-all ${isAssignedToOther ? 'opacity-50 grayscale pointer-events-none cursor-not-allowed bg-slate-50' : 'hover:border-brand-burgundy/30 hover:shadow-crisp-sm cursor-pointer bg-brand-straw/5'}`} 
        onClick={() => { if (!isAssignedToOther) setSelectedTask(task); }}
      >
        <div className="flex items-center gap-3">
          <span className="text-[10px] font-mono font-bold text-brand-burgundy bg-white px-1.5 py-0.5 rounded border border-brand-burgundy/20">{task.id || task.taskId}</span>
          <h4 className="text-xs font-bold text-brand-burgundy">{task.title}</h4>
          <Badge variant="outline" className={`text-[9px] px-1.5 py-0 rounded-sm uppercase ${task.type === 'BUG' ? 'border-red-500 text-red-600 bg-red-50' : task.type === 'USER_STORY' ? 'border-brand-mint text-brand-burgundy bg-brand-mint/20' : 'border-slate-400 text-slate-600 bg-slate-50'}`}>
            {task.type || task.epicName || 'TASK'}
          </Badge>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-[10px] font-bold uppercase text-brand-burgundy/70">{task.priority}</span>
          <div className="w-5 h-5 flex items-center justify-center bg-white border border-brand-burgundy/20 rounded-full text-[10px] font-mono font-bold text-brand-burgundy">{task.estimate || 0}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <header className="mb-4 flex flex-col gap-3 border-b border-brand-burgundy/15 pb-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/projects">
              <CrispButton customVariant="secondary" className="px-2 h-8">&larr; Back</CrispButton>
            </Link>
            <div>
              <h2 className="text-lg font-bold text-brand-burgundy tracking-tight uppercase">Sprint Workspace</h2>
              <p className="text-xs font-semibold text-brand-burgundy/70 uppercase tracking-widest mt-1">Project: {projectId}</p>
            </div>
          </div>
          <div className="flex gap-2 items-center">
            <div className="bg-brand-burgundy/5 p-1 rounded-sm border border-brand-burgundy/15 flex mr-4">
              <button 
                onClick={() => setView("BOARD")} 
                className={`px-3 py-1 text-[10px] font-bold uppercase transition-all rounded-sm ${view === "BOARD" ? "bg-white text-brand-burgundy shadow-crisp-sm border border-brand-burgundy/20" : "text-brand-burgundy/60 hover:text-brand-burgundy"}`}
              >
                Board
              </button>
              <button 
                onClick={() => setView("BACKLOG")} 
                className={`px-3 py-1 text-[10px] font-bold uppercase transition-all rounded-sm ${view === "BACKLOG" ? "bg-white text-brand-burgundy shadow-crisp-sm border border-brand-burgundy/20" : "text-brand-burgundy/60 hover:text-brand-burgundy"}`}
              >
                Backlog
              </button>
            </div>
            <CrispButton onClick={() => setShowMetricsModal(true)} customVariant="secondary" className="bg-brand-straw/30 text-brand-burgundy border-brand-burgundy/20 hover:bg-brand-straw/50">Analytics</CrispButton>
            {canInvite && <CrispButton onClick={() => setShowInvite(true)} customVariant="secondary">Invite Member</CrispButton>}
            {canCreateSprint && <CrispButton onClick={() => setShowSprintModal(true)} customVariant="primary">Create Sprint</CrispButton>}
          </div>
        </div>
      </header>

      {view === "BACKLOG" ? (
        <div className="flex-1 overflow-y-auto p-4 bg-white border border-brand-burgundy/15 rounded-sm space-y-6">
          {sprints.filter(s => s.status !== 'COMPLETED').map(sprint => {
            const sTasks = sprintTasksMap[sprint.id] || []
            return (
              <div key={sprint.id} className="border border-brand-burgundy/20 rounded-sm">
                <div className="bg-brand-mint/30 p-2 border-b border-brand-burgundy/10 flex justify-between items-center">
                  <div>
                    <h4 className="font-bold text-brand-burgundy text-xs uppercase">{sprint.name}</h4>
                    <span className="text-[10px] text-brand-burgundy/60 font-semibold uppercase">{sTasks.length} issues • {sprint.status}</span>
                  </div>
                  <div className="flex gap-2 items-center">
                    {canCreateTask && (
                      <CrispButton customVariant="secondary" className="h-6 px-2 text-[9px]" onClick={() => { setCreateTaskSprintId(sprint.id); setShowTaskModal(true); }}>+ Task</CrispButton>
                    )}
                    {sprint.status === 'PLANNED' && canStartSprint && (
                      <>
                        <CrispButton customVariant="secondary" className="h-6 px-2 text-[9px] text-red-500 hover:text-red-700" onClick={() => handleDeleteSprint(sprint.id)}>Delete</CrispButton>
                        <CrispButton customVariant="action" onClick={() => handleStartSprint(sprint.id)}>Start Sprint</CrispButton>
                      </>
                    )}
                    {sprint.status === 'ACTIVE' && canStartSprint && (
                      <CrispButton customVariant="secondary" onClick={() => handleCloseSprint(sprint.id)}>Close Sprint</CrispButton>
                    )}
                  </div>
                </div>
                <div className="p-2 space-y-1">
                  {sTasks.map(task => <BacklogTaskRow key={task.id} task={task} />)}
                  {sTasks.length === 0 && <p className="text-[10px] text-brand-burgundy/50 text-center py-2 uppercase font-bold">Plan a sprint by moving issues here</p>}
                </div>
              </div>
            )
          })}

          <div className="border border-brand-burgundy/20 rounded-sm mt-8">
            <div className="bg-brand-straw/30 p-2 border-b border-brand-burgundy/10 flex justify-between items-center">
              <div>
                <h4 className="font-bold text-brand-burgundy text-xs uppercase">Backlog</h4>
                <span className="text-[10px] text-brand-burgundy/60 font-semibold uppercase">{backlogTasks.length} issues</span>
              </div>
              {canCreateTask && <CrispButton onClick={() => { setCreateTaskSprintId(null); setShowTaskModal(true); }} customVariant="action" className="h-7 text-[10px]">New Task</CrispButton>}
            </div>
            <div className="p-2 space-y-1">
              {backlogTasks.map(task => <BacklogTaskRow key={task.id} task={task} />)}
              {backlogTasks.length === 0 && <p className="text-[10px] text-brand-burgundy/50 text-center py-2 uppercase font-bold">Backlog is empty</p>}
            </div>
          </div>
        </div>
      ) : !activeSprint ? (
        <div className="flex-1 flex flex-col items-center justify-center border-2 border-dashed border-brand-burgundy/15 rounded-sm bg-white/50">
          <p className="text-sm font-bold text-brand-burgundy/70 uppercase tracking-widest mb-4">No Active Sprint</p>
          <CrispButton onClick={() => setView("BACKLOG")} customVariant="action">Go to Backlog to Plan</CrispButton>
        </div>
      ) : loading ? (
        <div className="text-xs font-bold text-brand-burgundy uppercase">Loading board...</div>
      ) : (
        <div className="flex gap-4 items-start overflow-x-auto pb-4 flex-1 relative">
          <div onClick={(e) => {
            const target = e.target as HTMLElement;
            const card = target.closest('.cursor-grab');
            if (card) {
              const taskId = card.querySelector('span:nth-child(2)')?.textContent;
              const task = sprintTasks.find((t: any) => t.id === taskId || t.taskId === taskId);
              if (task) setSelectedTask(task);
            }
          }}>
            <SprintColumn columnTitle="To Do" taskCount={todoTasks.length} totalStoryPoints={todoTasks.reduce((acc: any, t: any) => acc + t.storyPoints, 0)} tasks={todoTasks} currentUserId={user?.id || user?.userId} />
          </div>
          <div onClick={(e) => {
             const target = e.target as HTMLElement;
             const card = target.closest('.cursor-grab');
             if (card) {
               const taskId = card.querySelector('span:nth-child(2)')?.textContent;
               const task = sprintTasks.find((t: any) => t.id === taskId || t.taskId === taskId);
               if (task) setSelectedTask(task);
             }
          }}>
            <SprintColumn columnTitle="In Progress" taskCount={inProgressTasks.length} totalStoryPoints={inProgressTasks.reduce((acc: any, t: any) => acc + t.storyPoints, 0)} tasks={inProgressTasks} currentUserId={user?.id || user?.userId} />
          </div>
          <div onClick={(e) => {
             const target = e.target as HTMLElement;
             const card = target.closest('.cursor-grab');
             if (card) {
               const taskId = card.querySelector('span:nth-child(2)')?.textContent;
               const task = sprintTasks.find((t: any) => t.id === taskId || t.taskId === taskId);
               if (task) setSelectedTask(task);
             }
          }}>
            <SprintColumn columnTitle="Done" taskCount={doneTasks.length} totalStoryPoints={doneTasks.reduce((acc: any, t: any) => acc + t.storyPoints, 0)} tasks={doneTasks} currentUserId={user?.id || user?.userId} />
          </div>
        </div>
      )}

      {/* Modals */}
      {showMetricsModal && <MetricsModal projectId={projectId!} sprintId={activeSprint?.id} onClose={() => setShowMetricsModal(false)} />}
      {showInvite && <InviteMemberModal projectId={projectId!} onClose={() => setShowInvite(false)} />}
      {showSprintModal && <CreateSprintModal projectId={projectId!} onClose={() => setShowSprintModal(false)} onCreated={() => fetchSprints()} />}
      {showTaskModal && <CreateTaskModal projectId={projectId!} sprintId={createTaskSprintId} onClose={() => setShowTaskModal(false)} onCreated={() => refreshData()} />}
      
      {taskToAssign && activeSprint && (
        <AssignBacklogTaskModal 
          task={taskToAssign} 
          projectId={projectId!} 
          sprintId={activeSprint.id} 
          onClose={() => setTaskToAssign(null)} 
          onAssigned={() => { setTaskToAssign(null); refreshData(); setSelectedTask(null); }} 
        />
      )}

      {/* Side Panel */}
      {selectedTask && (
        <>
          <div className="fixed inset-0 bg-brand-burgundy/10 z-30" onClick={() => setSelectedTask(null)} />
          <TaskPanel task={selectedTask} projectId={projectId!} sprints={sprints} onClose={() => setSelectedTask(null)} onUpdated={() => refreshData()} />
        </>
      )}
    </div>
  )
}
