import * as React from "react"
import { cn} from "../../utils/cs";

export interface ButtonProps
    extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: "default" | "destructive" | "outline" | "ghost" | "link"
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
    ({ className, variant = "default", ...props }, ref) => {
        return (
            <button
                className={cn(
                    "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 disabled:pointer-events-none disabled:opacity-50",
                    {
                        "bg-gray-900 text-gray-50 shadow hover:bg-gray-900/90":
                            variant === "default",
                        "bg-red-500 text-gray-50 shadow-sm hover:bg-red-500/90":
                            variant === "destructive",
                        "border border-gray-200 bg-white shadow-sm hover:bg-gray-100 hover:text-gray-900":
                            variant === "outline",
                        "hover:bg-gray-100 hover:text-gray-900": variant === "ghost",
                        "text-gray-900 underline-offset-4 hover:underline":
                            variant === "link",
                    },
                    className
                )}
                ref={ref}
                {...props}
            />
        )
    }
)
Button.displayName = "Button"

export { Button }