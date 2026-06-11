import { useState } from 'react'
import { CrispButton } from '@/components/CrispButton'
import { SprintColumn, AgileTask } from '@/components/SprintColumn'

export function Dashboard() {
  const [tasks] = useState<AgileTask[]>([
    {
      id: "TSK-001",
      title: "Design Database Schema",
      epicName: "Data Layer",
      storyPoints: 5,
      priority: "Critical"
    },
    {
      id: "TSK-002",
      title: "Implement Auth Gateway",
      epicName: "Security",
      storyPoints: 8,
      priority: "High"
    },
    {
      id: "TSK-003",
      title: "Create Frontend Layout",
      epicName: "UI/UX",
      storyPoints: 3,
      priority: "Medium"
    }
  ])

  return (
    <div className="min-h-screen bg-brand-straw/10 p-4">
      <header className="mb-6 flex items-center justify-between border-b border-brand-burgundy/15 pb-4">
        <div>
          <h1 className="text-xl font-bold text-brand-burgundy tracking-tight">AGILE WORKSPACE</h1>
          <p className="text-xs font-semibold text-brand-burgundy/70 uppercase tracking-widest mt-1">Project: E-Commerce Replatforming</p>
        </div>
        <div className="flex gap-2">
          <CrispButton customVariant="secondary">Configure</CrispButton>
          <CrispButton customVariant="action">New Task</CrispButton>
          <CrispButton customVariant="primary">Start Sprint</CrispButton>
        </div>
      </header>

      <main className="flex gap-4 items-start overflow-x-auto pb-4">
        <SprintColumn 
          columnTitle="To Do" 
          taskCount={3} 
          totalStoryPoints={16} 
          tasks={tasks} 
        />
        <SprintColumn 
          columnTitle="In Progress" 
          taskCount={0} 
          totalStoryPoints={0} 
          tasks={[]} 
        />
        <SprintColumn 
          columnTitle="Done" 
          taskCount={0} 
          totalStoryPoints={0} 
          tasks={[]} 
        />
      </main>
    </div>
  )
}
