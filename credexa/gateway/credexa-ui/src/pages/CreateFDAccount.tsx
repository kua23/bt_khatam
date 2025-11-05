import { useState } from 'react';
import { fdAccountApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle } from 'lucide-react';

export default function CreateFDAccount() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('standard');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [createdAccountNumber, setCreatedAccountNumber] = useState<string | null>(null);

  const [standardForm, setStandardForm] = useState({
    productCode: '',
    customerId: '',
    principalAmount: '',
    termMonths: '',
    branchCode: '',
    maturityInstruction: 'AUTO_CREDIT',
    autoRenewal: false,
    remarks: '',
  });

  const [customForm, setCustomForm] = useState({
    productCode: '',
    customerId: '',
    principalAmount: '',
    customTermMonths: '',
    customInterestRate: '',
    branchCode: '',
    maturityInstruction: 'AUTO_CREDIT',
    autoRenewal: false,
    remarks: '',
  });

  const handleStandardSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await fdAccountApi.createStandardAccount({
        productCode: standardForm.productCode,
        customerId: parseInt(standardForm.customerId),
        principalAmount: parseFloat(standardForm.principalAmount),
        termMonths: parseInt(standardForm.termMonths),
        branchCode: standardForm.branchCode || undefined,
        maturityInstruction: standardForm.maturityInstruction || undefined,
        autoRenewal: standardForm.autoRenewal,
        remarks: standardForm.remarks || undefined,
      });
      setSuccess(true);
      setCreatedAccountNumber(response.data.accountNumber);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create account. Please check your inputs.');
      console.error('Error creating account:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCustomSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await fdAccountApi.customizeAccount({
        productCode: customForm.productCode,
        customerId: parseInt(customForm.customerId),
        principalAmount: parseFloat(customForm.principalAmount),
        customTermMonths: parseInt(customForm.customTermMonths),
        customInterestRate: parseFloat(customForm.customInterestRate),
        branchCode: customForm.branchCode || undefined,
        maturityInstruction: customForm.maturityInstruction || undefined,
        autoRenewal: customForm.autoRenewal,
        remarks: customForm.remarks || undefined,
      });
      setSuccess(true);
      setCreatedAccountNumber(response.data.accountNumber);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create account. Please check your inputs.');
      console.error('Error creating account:', err);
    } finally {
      setLoading(false);
    }
  };

  if (success && createdAccountNumber) {
    return (
      <div className="p-8">
        <Card className="max-w-2xl mx-auto">
          <CardHeader className="text-center">
            <div className="flex justify-center mb-4">
              <CheckCircle className="text-green-500" size={64} />
            </div>
            <CardTitle className="text-2xl">Account Created Successfully!</CardTitle>
            <CardDescription>Your FD account has been opened</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-6 bg-blue-50 rounded-lg text-center">
              <div className="text-sm text-gray-600 mb-2">Account Number</div>
              <div className="text-3xl font-mono font-bold text-blue-600">
                {createdAccountNumber}
              </div>
            </div>
            <div className="flex gap-2">
              <Button onClick={() => navigate(`/accounts/${createdAccountNumber}`)} className="flex-1">
                View Account Details
              </Button>
              <Button onClick={() => navigate('/accounts')} variant="outline" className="flex-1">
                Back to Accounts
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" onClick={() => navigate('/accounts')}>
          <ArrowLeft size={20} />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Create FD Account</h1>
          <p className="text-gray-600 mt-2">Open a new fixed deposit account</p>
        </div>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Card className="max-w-4xl mx-auto">
        <CardHeader>
          <CardTitle>Account Details</CardTitle>
          <CardDescription>Choose between standard or customized FD account</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="standard">Standard Account</TabsTrigger>
              <TabsTrigger value="custom">Customized Account</TabsTrigger>
            </TabsList>

            <TabsContent value="standard" className="mt-6">
              <form onSubmit={handleStandardSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="productCode">Product Code *</Label>
                    <Input
                      id="productCode"
                      value={standardForm.productCode}
                      onChange={(e) => setStandardForm({ ...standardForm, productCode: e.target.value })}
                      required
                      placeholder="e.g., FD-STD-6M"
                    />
                  </div>
                  <div>
                    <Label htmlFor="customerId">Customer ID *</Label>
                    <Input
                      id="customerId"
                      type="number"
                      value={standardForm.customerId}
                      onChange={(e) => setStandardForm({ ...standardForm, customerId: e.target.value })}
                      required
                      placeholder="e.g., 1"
                    />
                  </div>
                  <div>
                    <Label htmlFor="principalAmount">Principal Amount (₹) *</Label>
                    <Input
                      id="principalAmount"
                      type="number"
                      step="0.01"
                      value={standardForm.principalAmount}
                      onChange={(e) => setStandardForm({ ...standardForm, principalAmount: e.target.value })}
                      required
                      placeholder="e.g., 100000"
                    />
                  </div>
                  <div>
                    <Label htmlFor="termMonths">Term (Months) *</Label>
                    <Input
                      id="termMonths"
                      type="number"
                      value={standardForm.termMonths}
                      onChange={(e) => setStandardForm({ ...standardForm, termMonths: e.target.value })}
                      required
                      placeholder="e.g., 12"
                    />
                  </div>
                  <div>
                    <Label htmlFor="branchCode">Branch Code</Label>
                    <Input
                      id="branchCode"
                      value={standardForm.branchCode}
                      onChange={(e) => setStandardForm({ ...standardForm, branchCode: e.target.value })}
                      placeholder="e.g., BR001"
                    />
                  </div>
                  <div>
                    <Label htmlFor="maturityInstruction">Maturity Instruction</Label>
                    <Select
                      value={standardForm.maturityInstruction}
                      onValueChange={(value) => setStandardForm({ ...standardForm, maturityInstruction: value })}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="AUTO_CREDIT">Auto Credit</SelectItem>
                        <SelectItem value="RENEW">Auto Renew</SelectItem>
                        <SelectItem value="NOTIFY">Notify Only</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    id="autoRenewal"
                    checked={standardForm.autoRenewal}
                    onChange={(e) => setStandardForm({ ...standardForm, autoRenewal: e.target.checked })}
                    className="w-4 h-4"
                  />
                  <Label htmlFor="autoRenewal" className="cursor-pointer">Auto Renewal</Label>
                </div>
                <div>
                  <Label htmlFor="remarks">Remarks</Label>
                  <Input
                    id="remarks"
                    value={standardForm.remarks}
                    onChange={(e) => setStandardForm({ ...standardForm, remarks: e.target.value })}
                    placeholder="Optional remarks"
                  />
                </div>
                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? 'Creating Account...' : 'Create Standard Account'}
                </Button>
              </form>
            </TabsContent>

            <TabsContent value="custom" className="mt-6">
              <form onSubmit={handleCustomSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="customProductCode">Product Code *</Label>
                    <Input
                      id="customProductCode"
                      value={customForm.productCode}
                      onChange={(e) => setCustomForm({ ...customForm, productCode: e.target.value })}
                      required
                      placeholder="e.g., FD-STD-6M"
                    />
                  </div>
                  <div>
                    <Label htmlFor="customCustomerId">Customer ID *</Label>
                    <Input
                      id="customCustomerId"
                      type="number"
                      value={customForm.customerId}
                      onChange={(e) => setCustomForm({ ...customForm, customerId: e.target.value })}
                      required
                      placeholder="e.g., 1"
                    />
                  </div>
                  <div>
                    <Label htmlFor="customPrincipalAmount">Principal Amount (₹) *</Label>
                    <Input
                      id="customPrincipalAmount"
                      type="number"
                      step="0.01"
                      value={customForm.principalAmount}
                      onChange={(e) => setCustomForm({ ...customForm, principalAmount: e.target.value })}
                      required
                      placeholder="e.g., 100000"
                    />
                  </div>
                  <div>
                    <Label htmlFor="customTermMonths">Custom Term (Months) *</Label>
                    <Input
                      id="customTermMonths"
                      type="number"
                      value={customForm.customTermMonths}
                      onChange={(e) => setCustomForm({ ...customForm, customTermMonths: e.target.value })}
                      required
                      placeholder="e.g., 18"
                    />
                  </div>
                  <div>
                    <Label htmlFor="customInterestRate">Custom Interest Rate (%) *</Label>
                    <Input
                      id="customInterestRate"
                      type="number"
                      step="0.01"
                      value={customForm.customInterestRate}
                      onChange={(e) => setCustomForm({ ...customForm, customInterestRate: e.target.value })}
                      required
                      placeholder="e.g., 7.25"
                    />
                  </div>
                  <div>
                    <Label htmlFor="customBranchCode">Branch Code</Label>
                    <Input
                      id="customBranchCode"
                      value={customForm.branchCode}
                      onChange={(e) => setCustomForm({ ...customForm, branchCode: e.target.value })}
                      placeholder="e.g., BR001"
                    />
                  </div>
                  <div className="col-span-2">
                    <Label htmlFor="customMaturityInstruction">Maturity Instruction</Label>
                    <Select
                      value={customForm.maturityInstruction}
                      onValueChange={(value) => setCustomForm({ ...customForm, maturityInstruction: value })}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="AUTO_CREDIT">Auto Credit</SelectItem>
                        <SelectItem value="RENEW">Auto Renew</SelectItem>
                        <SelectItem value="NOTIFY">Notify Only</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    id="customAutoRenewal"
                    checked={customForm.autoRenewal}
                    onChange={(e) => setCustomForm({ ...customForm, autoRenewal: e.target.checked })}
                    className="w-4 h-4"
                  />
                  <Label htmlFor="customAutoRenewal" className="cursor-pointer">Auto Renewal</Label>
                </div>
                <div>
                  <Label htmlFor="customRemarks">Remarks</Label>
                  <Input
                    id="customRemarks"
                    value={customForm.remarks}
                    onChange={(e) => setCustomForm({ ...customForm, remarks: e.target.value })}
                    placeholder="Optional remarks"
                  />
                </div>
                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? 'Creating Account...' : 'Create Customized Account'}
                </Button>
              </form>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
