"use client"

import { motion } from "framer-motion"

export function BrandPanel() {
  return (
    <div className="relative flex flex-col h-full bg-[#050505] overflow-hidden select-none">
      {/* Animated ambient glow */}
      <motion.div
        className="absolute rounded-full pointer-events-none"
        style={{
          width: 700,
          height: 700,
          top: "10%",
          left: "-20%",
          background:
            "radial-gradient(circle, rgba(201,169,110,0.10) 0%, transparent 65%)",
        }}
        animate={{ x: [0, 40, 0], y: [0, -30, 0] }}
        transition={{ duration: 14, repeat: Infinity, ease: "easeInOut" }}
      />

      {/* Decorative concentric circles — bottom right */}
      {[500, 680, 860].map((size, i) => (
        <motion.div
          key={size}
          className="absolute rounded-full border border-[#C9A96E] pointer-events-none"
          style={{
            width: size,
            height: size,
            bottom: -size / 2,
            right: -size / 2,
            opacity: 0.06 - i * 0.015,
          }}
          initial={{ scale: 0.85, opacity: 0 }}
          animate={{ scale: 1, opacity: 0.06 - i * 0.015 }}
          transition={{ duration: 1.6, delay: 0.1 * i, ease: "easeOut" }}
        />
      ))}

      {/* Ghost watermark */}
      <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
        <span
          className="text-[22vw] font-light tracking-widest text-white select-none"
          style={{
            fontFamily: "var(--font-cormorant)",
            opacity: 0.018,
            lineHeight: 1,
          }}
        >
          SŌLE
        </span>
      </div>

      {/* Content */}
      <div className="relative z-10 flex flex-col h-full p-14 justify-between">
        {/* Top wordmark */}
        <motion.div
          initial={{ opacity: 0, y: -12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, ease: "easeOut" }}
        >
          <span
            className="text-white/75 text-sm tracking-[0.5em] uppercase font-light"
            style={{ fontFamily: "var(--font-geist-sans)" }}
          >
            Sōle Studio
          </span>
        </motion.div>

        {/* Center statement */}
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.2, ease: [0.25, 0.1, 0.25, 1] }}
          className="space-y-7"
        >
          <div className="w-10 h-px bg-[#C9A96E]" />

          <h1
            className="text-[80px] font-light text-white leading-none tracking-[0.18em]"
            style={{ fontFamily: "var(--font-cormorant)" }}
          >
            SŌLE
          </h1>

          <div className="flex items-center gap-3">
            <div className="w-6 h-px bg-white/15" />
            <span
              className="text-white/55 text-[10px] tracking-[0.4em] uppercase"
              style={{ fontFamily: "var(--font-geist-sans)" }}
            >
              New Collection 2025
            </span>
            <div className="flex-1 h-px bg-white/10" />
          </div>

          <p
            className="text-white/60 text-2xl font-light leading-snug max-w-[320px] italic"
            style={{ fontFamily: "var(--font-cormorant)" }}
          >
            &ldquo;Crafted for those who refuse to stand still.&rdquo;
          </p>
        </motion.div>

        {/* Bottom */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.7, delay: 0.55 }}
          className="flex items-center justify-between"
        >
          <span
            className="text-white/45 text-[11px] tracking-[0.3em] uppercase"
            style={{ fontFamily: "var(--font-geist-sans)" }}
          >
            Premium Footwear · Est. 2001
          </span>
        </motion.div>
      </div>
    </div>
  )
}
