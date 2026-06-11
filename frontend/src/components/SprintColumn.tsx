import React from "react";
import { cn } from "@/lib/utils";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

export interface AgileTask {
  id: string;
  title: string;
  epicName: string;
  storyPoints: number;
  priority: string;
  assigneeId?: string | null;
}

interface SprintColumnProps {
  columnTitle: string;
  taskCount: number;
  totalStoryPoints: number;
  tasks: AgileTask[];
  currentUserId?: string;
}

export const SprintColumn: React.FC<SprintColumnProps> = ({
  columnTitle,
  taskCount,
  totalStoryPoints,
  tasks,
  currentUserId,
}) => {
  return (
    <div className="w-80 bg-brand-straw/20 border border-brand-burgundy/15 rounded flex flex-col h-[calc(100vh-180px)] select-none">
      
      {/* Structural Header Wrapper */}
      <div className="p-2.5 border-b border-brand-burgundy/15 bg-brand-mint/30 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="font-bold text-xs tracking-wider text-brand-burgundy uppercase">
            {columnTitle}
          </span>
          <Badge className="bg-brand-burgundy text-brand-straw font-mono rounded-sm text-[10px] px-1.5 py-0">
            {taskCount}
          </Badge>
        </div>
        <div className="text-[11px] font-medium text-brand-burgundy/70 bg-white border border-brand-burgundy/10 px-1.5 py-0.5 rounded-sm">
          SP: <span className="font-bold">{totalStoryPoints}</span>
        </div>
      </div>

      {/* Task List Workspace Body */}
      <CardContent className="p-2 flex-1 overflow-y-auto space-y-2 bg-gradient-to-b from-transparent to-brand-straw/5">
        {tasks.map((task) => {
          const isDisabled = task.assigneeId && currentUserId ? task.assigneeId !== currentUserId : false;

          return (
            <Card
              key={task.id}
              className={cn(
                "bg-white border border-brand-burgundy/15 rounded rounded-sm p-2.5 transition-all",
                isDisabled 
                  ? "opacity-50 grayscale cursor-not-allowed pointer-events-none" 
                  : "cursor-grab active:cursor-grabbing shadow-crisp-sm hover:shadow-[2px_2px_0px_0px_rgba(79,37,46,0.15)] hover:border-brand-burgundy/30"
              )}
            >
            {/* Metadata Line */}
            <div className="flex items-center justify-between gap-1 mb-1">
              <span className="text-[9px] font-extrabold tracking-widest uppercase text-brand-amber truncate max-w-[70%]">
                {task.epicName}
              </span>
              <span className="text-[10px] font-mono text-brand-burgundy/50 font-semibold">
                {task.id}
              </span>
            </div>

            {/* Task Main Content Heading */}
            <h4 className="text-xs font-medium text-brand-burgundy leading-snug line-clamp-2 mb-2.5">
              {task.title}
            </h4>

            {/* Interactive Footer metrics */}
            <div className="flex items-center justify-between pt-2 border-t border-dashed border-brand-burgundy/10">
              <div className="flex items-center gap-1.5">
                <span
                  className={cn(
                    "text-[9px] font-bold px-1.5 py-0.5 rounded-sm border uppercase",
                    task.priority === "Critical" && "bg-red-50 text-red-700 border-red-200",
                    task.priority === "High" && "bg-orange-50 text-brand-burgundy border-brand-amber/30",
                    task.priority === "Medium" && "bg-brand-mint/40 text-brand-burgundy border-brand-mint",
                    task.priority === "Low" && "bg-slate-50 text-slate-500 border-slate-200"
                  )}
                >
                  {task.priority}
                </span>
              </div>
              
              {/* Point Allocation Indicator */}
              <div className="w-5 h-5 flex items-center justify-center bg-brand-straw/60 border border-brand-burgundy/20 rounded-full text-[10px] font-mono font-bold text-brand-burgundy">
                {task.storyPoints}
              </div>
            </div>
          </Card>
          );
        })}
      </CardContent>
    </div>
  );
};
