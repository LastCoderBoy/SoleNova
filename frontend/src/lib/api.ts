const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"

export interface AuthUser {
  id: string
  email: string
  firstName: string
  lastName: string
  role: "USER" | "ADMIN"
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: AuthUser
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    headers: { "Content-Type": "application/json", ...options.headers },
    ...options,
  })
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Request failed" }))
    throw new Error(error.message ?? "Something went wrong")
  }
  return res.json()
}

export const authApi = {
  login: (email: string, password: string) =>
    request<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  register: (firstName: string, lastName: string, email: string, password: string) =>
    request<AuthResponse>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ firstName, lastName, email, password }),
    }),
}
