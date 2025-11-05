import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Building2, Loader2, CheckCircle2 } from 'lucide-react';
import { authApi } from '@/services/api';
import type { BankConfigResponse } from '@/types';

export default function Login() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('login');
  const [bankConfig, setBankConfig] = useState<BankConfigResponse | null>(null);
  
  // Login state
  const [loginForm, setLoginForm] = useState({
    usernameOrEmailOrMobile: '',
    password: '',
  });
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  // Register state
  const [registerForm, setRegisterForm] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    mobileNumber: '',
    preferredLanguage: 'en',
    preferredCurrency: 'USD',
  });
  const [registerLoading, setRegisterLoading] = useState(false);
  const [registerError, setRegisterError] = useState<string | null>(null);
  const [registerSuccess, setRegisterSuccess] = useState(false);

  useEffect(() => {
    fetchBankConfig();
  }, []);

  const fetchBankConfig = async () => {
    try {
      const response = await authApi.getBankConfig();
      setBankConfig(response.data.data);
    } catch (error) {
      console.error('Failed to fetch bank config:', error);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginLoading(true);
    setLoginError(null);

    try {
      console.log('Attempting login with:', loginForm.usernameOrEmailOrMobile);
      const response = await authApi.login(loginForm);
      console.log('Login response:', response.data);
      const { data } = response.data;
      
      if (!data || !data.token) {
        throw new Error('Invalid response from server');
      }
      
      // Store authentication data
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId.toString());
      localStorage.setItem('username', data.username);
      localStorage.setItem('userRoles', JSON.stringify(data.roles));
      
      console.log('Login successful, navigating to dashboard');
      navigate('/');
    } catch (error: unknown) {
      console.error('Login error:', error);
      const err = error as { response?: { data?: { error?: string; message?: string } } };
      const errorMsg = err.response?.data?.error || err.response?.data?.message || 'Login failed. Please check your credentials.';
      setLoginError(errorMsg);
    } finally {
      setLoginLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegisterLoading(true);
    setRegisterError(null);
    setRegisterSuccess(false);

    // Validate passwords match
    if (registerForm.password !== registerForm.confirmPassword) {
      setRegisterError('Passwords do not match');
      setRegisterLoading(false);
      return;
    }

    // Validate password length
    if (registerForm.password.length < 8) {
      setRegisterError('Password must be at least 8 characters long');
      setRegisterLoading(false);
      return;
    }

    try {
      const { confirmPassword: _, ...registerData } = registerForm;
      console.log('Attempting registration with:', registerData);
      const response = await authApi.register(registerData);
      console.log('Registration response:', response.data);
      
      setRegisterSuccess(true);
      setRegisterForm({
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        mobileNumber: '',
        preferredLanguage: 'en',
        preferredCurrency: 'USD',
      });
      
      // Switch to login tab after 2 seconds
      setTimeout(() => {
        setActiveTab('login');
        setRegisterSuccess(false);
      }, 2000);
    } catch (error: unknown) {
      console.error('Registration error:', error);
      const err = error as { response?: { data?: { error?: string; message?: string } } };
      const errorMsg = err.response?.data?.error || err.response?.data?.message || 'Registration failed. Please try again.';
      setRegisterError(errorMsg);
    } finally {
      setRegisterLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-4 overflow-auto">
      <Card className="w-full max-w-md shadow-xl my-8">
        <CardHeader className="space-y-1 text-center pb-4">
          <div className="flex justify-center mb-4">
            <div className="p-3 bg-gradient-to-br from-blue-600 to-indigo-600 rounded-full shadow-lg">
              <Building2 className="text-white" size={40} />
            </div>
          </div>
          <CardTitle className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
            {bankConfig?.bankName || 'Credexa FD'}
          </CardTitle>
          <CardDescription className="text-base text-gray-600">
            Fixed Deposit Management System
          </CardDescription>
        </CardHeader>
        
        <CardContent className="max-h-[calc(100vh-300px)] overflow-y-auto">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-2 mb-6">
              <TabsTrigger value="login" className="text-sm font-medium">Login</TabsTrigger>
              <TabsTrigger value="register" className="text-sm font-medium">Register</TabsTrigger>
            </TabsList>

            {/* Login Tab */}
            <TabsContent value="login" className="space-y-4">
              <form onSubmit={handleLogin} className="space-y-4">
                {loginError && (
                  <Alert variant="destructive">
                    <AlertDescription>{loginError}</AlertDescription>
                  </Alert>
                )}

                <div className="space-y-2">
                  <Label htmlFor="usernameOrEmailOrMobile">Username, Email or Mobile</Label>
                  <Input
                    id="usernameOrEmailOrMobile"
                    type="text"
                    placeholder="Enter username, email or mobile"
                    value={loginForm.usernameOrEmailOrMobile}
                    onChange={(e) => setLoginForm({ ...loginForm, usernameOrEmailOrMobile: e.target.value })}
                    required
                    autoFocus
                    className="h-11"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="password">Password</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="Enter your password"
                    value={loginForm.password}
                    onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                    required
                    className="h-11"
                  />
                </div>

                <Button type="submit" className="w-full h-11 text-base font-medium" disabled={loginLoading}>
                  {loginLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Signing in...
                    </>
                  ) : (
                    'Sign In'
                  )}
                </Button>

                <div className="text-center text-sm text-gray-600 mt-4 p-3 bg-blue-50 rounded-lg">
                  <p className="font-medium mb-1">New User?</p>
                  <p className="text-xs">Switch to the Register tab to create an account</p>
                </div>
              </form>
            </TabsContent>

            {/* Register Tab */}
            <TabsContent value="register" className="space-y-4 mt-0">
              {registerSuccess ? (
                <Alert className="bg-green-50 border-green-200">
                  <CheckCircle2 className="h-4 w-4 text-green-600" />
                  <AlertDescription className="text-green-800 font-medium">
                    Registration successful! Redirecting to login...
                  </AlertDescription>
                </Alert>
              ) : (
                <form onSubmit={handleRegister} className="space-y-4 pb-2">
                  {registerError && (
                    <Alert variant="destructive">
                      <AlertDescription>{registerError}</AlertDescription>
                    </Alert>
                  )}

                  <div className="space-y-2">
                    <Label htmlFor="reg-username">Username *</Label>
                    <Input
                      id="reg-username"
                      type="text"
                      placeholder="Choose a username (min 3 chars)"
                      value={registerForm.username}
                      onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })}
                      required
                      minLength={3}
                      className="h-11"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="reg-email">Email *</Label>
                    <Input
                      id="reg-email"
                      type="email"
                      placeholder="your.email@example.com"
                      value={registerForm.email}
                      onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                      required
                      className="h-11"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="reg-mobile">Mobile Number</Label>
                    <Input
                      id="reg-mobile"
                      type="tel"
                      placeholder="1234567890 (optional)"
                      value={registerForm.mobileNumber}
                      onChange={(e) => setRegisterForm({ ...registerForm, mobileNumber: e.target.value })}
                      minLength={10}
                      maxLength={15}
                      className="h-11"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="reg-password">Password *</Label>
                    <Input
                      id="reg-password"
                      type="password"
                      placeholder="Min 8 characters"
                      value={registerForm.password}
                      onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                      required
                      minLength={8}
                      className="h-11"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="reg-confirm-password">Confirm Password *</Label>
                    <Input
                      id="reg-confirm-password"
                      type="password"
                      placeholder="Re-enter password"
                      value={registerForm.confirmPassword}
                      onChange={(e) => setRegisterForm({ ...registerForm, confirmPassword: e.target.value })}
                      required
                      className="h-11"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="reg-language">Language</Label>
                      <Select
                        value={registerForm.preferredLanguage}
                        onValueChange={(value) => setRegisterForm({ ...registerForm, preferredLanguage: value })}
                      >
                        <SelectTrigger className="h-11">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="en">English</SelectItem>
                          <SelectItem value="es">Spanish</SelectItem>
                          <SelectItem value="fr">French</SelectItem>
                          <SelectItem value="hi">Hindi</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="reg-currency">Currency</Label>
                      <Select
                        value={registerForm.preferredCurrency}
                        onValueChange={(value) => setRegisterForm({ ...registerForm, preferredCurrency: value })}
                      >
                        <SelectTrigger className="h-11">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="USD">USD ($)</SelectItem>
                          <SelectItem value="EUR">EUR (€)</SelectItem>
                          <SelectItem value="INR">INR (₹)</SelectItem>
                          <SelectItem value="GBP">GBP (£)</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <Button type="submit" className="w-full h-11 text-base font-medium" disabled={registerLoading}>
                    {registerLoading ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Creating Account...
                      </>
                    ) : (
                      'Create Account'
                    )}
                  </Button>
                </form>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
