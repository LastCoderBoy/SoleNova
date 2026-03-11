import { BrandPanel } from "@/components/auth/brand-panel"

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen bg-[#0A0A0A]">
      {/* Left — brand panel, sticky on desktop */}
      <div className="sticky top-0 h-screen hidden lg:block w-1/2 xl:w-[55%]">
        <BrandPanel />
      </div>

      {/* Right — form panel */}
      <div className="flex-1 flex flex-col min-h-screen">
        {/* Mobile brand header */}
        <div className="lg:hidden flex items-center px-8 pt-10 pb-4">
          <span
            className="text-white text-xl tracking-[0.4em] uppercase font-light"
            style={{ fontFamily: "var(--font-cormorant)" }}
          >
            SŌLE
          </span>
        </div>

        {/* Centered form */}
        <div className="flex-1 flex items-center justify-center px-8 sm:px-12 py-12">
          <div className="w-full max-w-[420px]">{children}</div>
        </div>
      </div>
    </div>
  )
}
