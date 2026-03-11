"use client"

import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { motion, AnimatePresence } from "framer-motion"
import { Eye, EyeOff, ArrowRight, Loader2 } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"

import { registerSchema, type RegisterFormData } from "@/lib/validations/auth"
import { authApi } from "@/lib/api"
import { useAuthStore } from "@/store/auth-store"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { cn } from "@/lib/utils"

const container = {
  hidden: {},
  show: { transition: { staggerChildren: 0.08 } },
}

const item = {
  hidden: { opacity: 0, y: 18 },
  show: { opacity: 1, y: 0, transition: { duration: 0.5, ease: "easeOut" as const } },
}

function FormField({
  label,
  error,
  children,
}: {
  label: string
  error?: string
  children: React.ReactNode
}) {
  return (
    <motion.div variants={item} className="space-y-2">
      <Label className="text-white/75 text-xs tracking-[0.12em] uppercase font-normal">
        {label}
      </Label>
      {children}
      <AnimatePresence>
        {error && (
          <motion.p
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="text-red-400/80 text-xs"
          >
            {error}
          </motion.p>
        )}
      </AnimatePresence>
    </motion.div>
  )
}

export function RegisterForm() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)
  const router = useRouter()
  const setAuth = useAuthStore((s) => s.setAuth)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({ resolver: zodResolver(registerSchema) })

  const onSubmit = async ({ firstName, lastName, email, password }: RegisterFormData) => {
    setServerError(null)
    try {
      const res = await authApi.register(firstName, lastName, email, password)
      setAuth(res.user, res.accessToken)
      router.push("/")
    } catch (err) {
      setServerError(err instanceof Error ? err.message : "Registration failed. Please try again.")
    }
  }

  const inputClass = (hasError?: boolean) =>
    cn(
      "h-13 rounded-sm bg-white/[0.04] border-white/10 text-white text-base",
      "placeholder:text-white/35 focus-visible:border-[#C9A96E]",
      "focus-visible:ring-1 focus-visible:ring-[#C9A96E]/20 transition-colors",
      hasError && "border-red-500/40"
    )

  return (
    <motion.div variants={container} initial="hidden" animate="show" className="w-full space-y-7">
      {/* Heading */}
      <motion.div variants={item} className="space-y-2">
        <h2
          className="text-5xl font-light text-white tracking-tight"
          style={{ fontFamily: "var(--font-cormorant)" }}
        >
          Create account
        </h2>
        <p className="text-white/60 text-base">Join the SŌLE community</p>
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
        {/* Name row */}
        <div className="grid grid-cols-2 gap-3">
          <FormField label="First Name" error={errors.firstName?.message}>
            <Input
              placeholder="John"
              autoComplete="given-name"
              className={inputClass(!!errors.firstName)}
              {...register("firstName")}
            />
          </FormField>
          <FormField label="Last Name" error={errors.lastName?.message}>
            <Input
              placeholder="Doe"
              autoComplete="family-name"
              className={inputClass(!!errors.lastName)}
              {...register("lastName")}
            />
          </FormField>
        </div>

        {/* Email */}
        <FormField label="Email Address" error={errors.email?.message}>
          <Input
            type="email"
            placeholder="you@example.com"
            autoComplete="email"
            className={inputClass(!!errors.email)}
            {...register("email")}
          />
        </FormField>

        {/* Password */}
        <FormField label="Password" error={errors.password?.message}>
          <div className="relative">
            <Input
              type={showPassword ? "text" : "password"}
              placeholder="••••••••"
              autoComplete="new-password"
              className={cn(inputClass(!!errors.password), "pr-11")}
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
        </FormField>

        {/* Confirm Password */}
        <FormField label="Confirm Password" error={errors.confirmPassword?.message}>
          <div className="relative">
            <Input
              type={showConfirm ? "text" : "password"}
              placeholder="••••••••"
              autoComplete="new-password"
              className={cn(inputClass(!!errors.confirmPassword), "pr-11")}
              {...register("confirmPassword")}
            />
            <button
              type="button"
              onClick={() => setShowConfirm((v) => !v)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-white/45 hover:text-white/70 transition-colors"
              tabIndex={-1}
            >
              {showConfirm ? <EyeOff size={15} /> : <Eye size={15} />}
            </button>
          </div>
        </FormField>

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
                Create Account <ArrowRight size={15} />
              </>
            )}
          </Button>
        </motion.div>
      </form>

      {/* Login link */}
      <motion.p variants={item} className="text-center text-white/55 text-sm">
        Already have an account?{" "}
        <Link href="/login" className="text-[#C9A96E]/80 hover:text-[#C9A96E] transition-colors">
          Sign in
        </Link>
      </motion.p>
    </motion.div>
  )
}
