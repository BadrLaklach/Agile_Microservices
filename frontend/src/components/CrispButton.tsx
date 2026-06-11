import React from "react";
import { Button, type ButtonProps } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export interface CrispButtonProps extends ButtonProps {
  customVariant?: "primary" | "secondary" | "action";
}

export const CrispButton = React.forwardRef<HTMLButtonElement, CrispButtonProps>(
  ({ className, customVariant = "primary", children, ...props }, ref) => {
    return (
      <Button
        ref={ref}
        className={cn(
          // Essential crisp alignments: hard tracking, flat caps, precise transitions
          "rounded-sm text-xs font-semibold uppercase tracking-wider h-8 px-4 border transition-all duration-75 select-none",
          "focus-visible:ring-1 focus-visible:ring-brand-burgundy focus-visible:ring-offset-1 outline-none",
          "active:translate-x-[1px] active:translate-y-[1px] active:shadow-none",
          
          customVariant === "primary" && [
            "bg-brand-burgundy text-[#FFF7C5] border-brand-burgundy",
            "shadow-crisp-amber hover:bg-brand-burgundy/95"
          ],
          
          customVariant === "secondary" && [
            "bg-brand-mint text-brand-burgundy border-brand-burgundy/20",
            "shadow-[2px_2px_0px_0px_rgba(79,37,46,1)] hover:bg-brand-mint/80"
          ],
          
          customVariant === "action" && [
            "bg-brand-amber text-brand-burgundy border-brand-burgundy",
            "shadow-[2px_2px_0px_0px_rgba(79,37,46,1)] hover:bg-brand-amber/95"
          ],
          className
        )}
        {...props}
      >
        {children}
      </Button>
    );
  }
);

CrispButton.displayName = "CrispButton";
