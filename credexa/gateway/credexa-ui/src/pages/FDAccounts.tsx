import { useEffect, useState } from 'react';
import { fdAccountApi } from '@/services/api';
import type { FdAccount } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Plus, Search, Eye } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';

export default function FDAccounts() {
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState<FdAccount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchAccounts();
  }, []);

  const fetchAccounts = async () => {
    try {
      setLoading(true);
      setError(null);
      // For now, we'll try to fetch by a sample customer ID
      // In production, you'd have a better way to list all accounts
      const response = await fdAccountApi.getAccountsByCustomer(1);
      setAccounts(response.data);
    } catch (err) {
      setError('Failed to fetch accounts. Please ensure all services are running.');
      console.error('Error fetching accounts:', err);
      setAccounts([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredAccounts = accounts.filter((account) =>
    account.accountNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    account.productCode.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { variant: 'default' | 'secondary' | 'destructive'; label: string }> = {
      ACTIVE: { variant: 'default', label: 'Active' },
      MATURED: { variant: 'secondary', label: 'Matured' },
      CLOSED: { variant: 'destructive', label: 'Closed' },
    };
    const config = statusMap[status] || { variant: 'secondary', label: status };
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-lg text-gray-600">Loading accounts...</div>
      </div>
    );
  }

  return (
    <div className="p-8 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">FD Accounts</h1>
          <p className="text-gray-600 mt-2">Manage fixed deposit accounts</p>
        </div>
        <Button
          className="flex items-center gap-2"
          onClick={() => navigate('/accounts/create')}
        >
          <Plus size={16} />
          Create FD Account
        </Button>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Account List</CardTitle>
          <div className="flex items-center gap-2 mt-4">
            <Search className="text-gray-400" size={20} />
            <Input
              placeholder="Search accounts..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="max-w-sm"
            />
          </div>
        </CardHeader>
        <CardContent>
          {filteredAccounts.length === 0 ? (
            <div className="text-center text-gray-500 py-8">
              No accounts found. Create your first FD account!
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Account Number</TableHead>
                  <TableHead>Product</TableHead>
                  <TableHead>Principal</TableHead>
                  <TableHead>Interest Rate</TableHead>
                  <TableHead>Maturity Amount</TableHead>
                  <TableHead>Maturity Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredAccounts.map((account) => (
                  <TableRow key={account.accountNumber}>
                    <TableCell className="font-mono font-medium">
                      {account.accountNumber}
                    </TableCell>
                    <TableCell>{account.productCode}</TableCell>
                    <TableCell>₹{account.principalAmount.toLocaleString()}</TableCell>
                    <TableCell className="text-green-600 font-semibold">
                      {account.interestRate}%
                    </TableCell>
                    <TableCell className="font-semibold">
                      ₹{account.maturityAmount.toLocaleString()}
                    </TableCell>
                    <TableCell>
                      {format(new Date(account.maturityDate), 'dd MMM yyyy')}
                    </TableCell>
                    <TableCell>{getStatusBadge(account.accountStatus)}</TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => navigate(`/accounts/${account.accountNumber}`)}
                      >
                        <Eye size={16} />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
