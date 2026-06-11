import { useState } from "react";
import { CrispButton } from "@/components/CrispButton";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";

export function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      const res = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (res.ok) {
        const data = await res.json();
        login(data);
        navigate("/dashboard");
      } else {
        const data = await res.json();
        setError(data.detail || "Login failed");
      }
    } catch (err) {
      setError("Network error occurred");
    }
  };

  return (
    <div className="min-h-screen bg-brand-straw/10 flex items-center justify-center p-4">
      <div className="w-full max-w-sm bg-white border border-brand-burgundy/15 p-4 rounded-sm shadow-crisp-md">
        <div className="mb-4 border-b border-brand-burgundy/15 pb-2">
          <h1 className="text-lg font-bold text-brand-burgundy tracking-tight uppercase">System Login</h1>
          <p className="text-[10px] font-semibold text-brand-burgundy/70 uppercase tracking-widest mt-0.5">Agile Workspace Access</p>
        </div>
        
        {error && (
          <div className="mb-3 p-2 bg-red-50 border border-brand-amber/30 text-red-700 text-xs font-semibold rounded-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-3">
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase tracking-wider text-brand-burgundy">Email Address</Label>
            <Input 
              type="email" 
              required 
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="h-8 rounded-sm text-xs border-brand-burgundy/20 focus-visible:ring-1 focus-visible:ring-brand-burgundy shadow-crisp-sm" 
              placeholder="admin@agile.local"
            />
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase tracking-wider text-brand-burgundy">Password</Label>
            <Input 
              type="password" 
              required 
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="h-8 rounded-sm text-xs border-brand-burgundy/20 focus-visible:ring-1 focus-visible:ring-brand-burgundy shadow-crisp-sm" 
            />
          </div>
          <div className="pt-2 flex items-center justify-between">
            <Link to="/signup" className="text-[10px] font-bold uppercase text-brand-burgundy/70 hover:text-brand-burgundy transition-colors underline underline-offset-2">
              Create Account
            </Link>
            <CrispButton type="submit" customVariant="primary">Authenticate</CrispButton>
          </div>
        </form>
      </div>
    </div>
  );
}
