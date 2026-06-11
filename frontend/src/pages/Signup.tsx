import { useState } from "react";
import { CrispButton } from "@/components/CrispButton";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";

export function Signup() {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    role: "DEV"
  });
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      const res = await fetch("/api/v1/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });
      if (res.ok) {
        const data = await res.json();
        login(data);
        navigate("/dashboard");
      } else {
        const data = await res.json();
        setError(data.detail || "Registration failed");
      }
    } catch (err) {
      setError("Network error occurred");
    }
  };

  return (
    <div className="min-h-screen bg-brand-straw/10 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white border border-brand-burgundy/15 p-4 rounded-sm shadow-crisp-md">
        <div className="mb-4 border-b border-brand-burgundy/15 pb-2">
          <h1 className="text-lg font-bold text-brand-burgundy tracking-tight uppercase">User Registration</h1>
          <p className="text-[10px] font-semibold text-brand-burgundy/70 uppercase tracking-widest mt-0.5">Provision New Identity</p>
        </div>
        
        {error && (
          <div className="mb-3 p-2 bg-red-50 border border-brand-amber/30 text-red-700 text-xs font-semibold rounded-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSignup} className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label className="text-[10px] font-bold uppercase tracking-wider text-brand-burgundy">First Name</Label>
              <Input 
                required 
                value={formData.firstName}
                onChange={e => setFormData({...formData, firstName: e.target.value})}
                className="h-8 rounded-sm text-xs border-brand-burgundy/20 focus-visible:ring-1 focus-visible:ring-brand-burgundy shadow-crisp-sm" 
              />
            </div>
            <div className="space-y-1">
              <Label className="text-[10px] font-bold uppercase tracking-wider text-brand-burgundy">Last Name</Label>
              <Input 
                required 
                value={formData.lastName}
                onChange={e => setFormData({...formData, lastName: e.target.value})}
                className="h-8 rounded-sm text-xs border-brand-burgundy/20 focus-visible:ring-1 focus-visible:ring-brand-burgundy shadow-crisp-sm" 
              />
            </div>
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase tracking-wider text-brand-burgundy">Email Address</Label>
            <Input 
              type="email" 
              required 
              value={formData.email}
              onChange={e => setFormData({...formData, email: e.target.value})}
              className="h-8 rounded-sm text-xs border-brand-burgundy/20 focus-visible:ring-1 focus-visible:ring-brand-burgundy shadow-crisp-sm" 
            />
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase tracking-wider text-brand-burgundy">Password</Label>
            <Input 
              type="password" 
              required 
              value={formData.password}
              onChange={e => setFormData({...formData, password: e.target.value})}
              className="h-8 rounded-sm text-xs border-brand-burgundy/20 focus-visible:ring-1 focus-visible:ring-brand-burgundy shadow-crisp-sm" 
            />
          </div>
          <div className="space-y-1">
            <Label className="text-[10px] font-bold uppercase tracking-wider text-brand-burgundy">System Role</Label>
            <select 
              value={formData.role}
              onChange={e => setFormData({...formData, role: e.target.value})}
              className="flex h-8 w-full rounded-sm border border-brand-burgundy/20 bg-transparent px-2 py-1 text-xs shadow-crisp-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand-burgundy"
            >
              <option value="ADMIN">System Administrator (ADMIN)</option>
              <option value="PO">Product Owner (PO)</option>
              <option value="SM">Scrum Master (SM)</option>
              <option value="DEV">Developer (DEV)</option>
              <option value="MA">Manager (MA)</option>
            </select>
          </div>
          <div className="pt-2 flex items-center justify-between">
            <Link to="/login" className="text-[10px] font-bold uppercase text-brand-burgundy/70 hover:text-brand-burgundy transition-colors underline underline-offset-2">
              Back to Login
            </Link>
            <CrispButton type="submit" customVariant="action">Register User</CrispButton>
          </div>
        </form>
      </div>
    </div>
  );
}
