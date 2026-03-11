"use client"

import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { motion, AnimatePresence } from "framer-motion"
import { Eye, EyeOff, ArrowRight, Loader2 } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"

import { loginSchema, type LoginFormData } from "@/lib/validations/auth"
import { authApi } from "@/lib/api"
import { useAuthStore } from "@/store/auth-store"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { cn } from "@/lib/utils"

const container = {
  hidden: {},
  show: { transition: { staggerChildren: 0.09 } },
}

const item = {
  hidden: { opacity: 0, y: 18 },
  show: { opacity: 1, y: 0, transition: { duration: 0.5, ease: "easeOut" as const } },
}

export function LoginForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)
  const router = useRouter()
  const setAuth = useAuthStore((s) => s.setAuth)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({ resolver: zodResolver(loginSchema) })

  const onSubmit = async ({ email, password }: LoginFormData) => {
    setServerError(null)
    try {
      const res = await authApi.login(email, password)
      setAuth(res.user, res.accessToken)
      router.push("/")
    } catch (err) {
      setServerError(err instanceof Error ? err.message : "Login failed. Please try again.")
    }
  }

  return (
    <motion.div variants={container} initial="hidden" animate="show" className="w-full space-y-8">
      {/* Heading */}
      <motion.div variants={item} className="space-y-2">
        <h2
          className="text-5xl font-light text-white tracking-tight"
          style={{ fontFamily: "var(--font-cormorant)" }}
        >
          Welcome back
        </h2>
        <p className="text-white/60 text-base">Sign in to continue your journey</p>
      </motion.div>

      {/* Server error */}
      <AnimatePresence>
        {serverError && (
          <motion.div
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
            className="px-4 py-3 rounded-sm bg-red-500/10 border border-red-500/20 text-red-400 text-sm"
          >
            {serverError}
          </motion.div>
        )}
      </AnimatePresence>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
        {/* Email */}
        <motion.div variants={item} className="space-y-2">
          <Label className="text-white/75 text-xs tracking-[0.12em] uppercase font-normal">
            Email Address
          </Label>
          <Input
            type="email"
            placeholder="you@example.com"
            autoComplete="email"
            className={cn(
              "h-13 rounded-sm bg-white/[0.04] border-white/10 text-white text-base",
              "placeholder:text-white/35 focus-visible:border-[#C9A96E]",
              "focus-visible:ring-1 focus-visible:ring-[#C9A96E]/20 transition-colors",
              errors.email && "border-red-500/40"
            )}
            {...register("email")}
          />
          {errors.email && (
            <motion.p initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-red-400/80 text-xs">
              {errors.email.message}
            </motion.p>
          )}
        </motion.div>

        {/* Password */}
        <motion.div variants={item} className="space-y-2">
          <Label className="text-white/75 text-xs tracking-[0.12em] uppercase font-normal">
            Password
          </Label>
          <div className="relative">
            <Input
              type={showPassword ? "text" : "password"}
              placeholder="••••••••"
              autoComplete="current-password"
              className={cn(
                "h-13 rounded-sm bg-white/[0.04] border-white/10 text-white text-base pr-11",
                "placeholder:text-white/35 focus-visible:border-[#C9A96E]",
                "focus-visible:ring-1 focus-visible:ring-[#C9A96E]/20 transition-colors",
                errors.password && "border-red-500/40"
              )}
              {...register("password")}
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-white/45 hover:text-white/70 transition-colors"
              tabIndex={-1}
            >
              {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
            </button>
          </div>
          {errors.password && (
            <motion.p initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-red-400/80 text-xs">
              {errors.password.message}
            </motion.p>
          )}
        </motion.div>

        {/* Submit */}
        <motion.div variants={item} className="pt-1">
          <Button
            type="submit"
            disabled={isSubmitting}
            className={cn(
              "w-full h-12 rounded-sm text-sm font-medium tracking-[0.12em] uppercase",
              "bg-[#C9A96E] text-[#0A0A0A] hover:bg-[#D4B87A] transition-colors",
              "disabled:opacity-50 flex items-center justify-center gap-2"
            )}
          >
            {isSubmitting ? (
              <Loader2 size={15} className="animate-spin" />
            ) : (
              <>
                Sign In <ArrowRight size={15} />
              </>
            )}
          </Button>
        </motion.div>
      </form>

      {/* Register link */}
      <motion.p variants={item} className="text-center text-white/55 text-sm">
        Don&apos;t have an account?{" "}
        <Link href="/register" className="text-[#C9A96E]/80 hover:text-[#C9A96E] transition-colors">
          Create one
        </Link>
      </motion.p>
    </motion.div>
  )
}
