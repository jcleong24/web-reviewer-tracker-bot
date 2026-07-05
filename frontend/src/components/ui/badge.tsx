import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset transition-colors",
  {
    variants: {
      variant: {
        default: "bg-primary/15 text-primary ring-primary/25",
        secondary: "bg-secondary text-secondary-foreground ring-transparent",
        success: "bg-emerald-500/15 text-emerald-300 ring-emerald-500/25",
        info: "bg-sky-500/15 text-sky-300 ring-sky-500/25",
        warning: "bg-amber-500/15 text-amber-300 ring-amber-500/25",
        destructive: "bg-red-500/15 text-red-300 ring-red-500/25",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return <span className={cn(badgeVariants({ variant }), className)} {...props} />;
}

export { Badge, badgeVariants };
