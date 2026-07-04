import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/** ShadCN class-name helper: merge conditional classes and dedupe Tailwind utilities. */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
