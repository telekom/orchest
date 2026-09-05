/**
 * Resolve the MSAL active account, restoring from cache when needed.
 * After a cold start MSAL may have accounts in cache but no active account set.
 */
export function resolveActiveAccount<T>(
  getActiveAccount: () => T | null,
  getAllAccounts: () => T[],
  setActiveAccount: (account: T) => void
): T | null {
  const active = getActiveAccount();
  if (active) {
    return active;
  }

  const accounts = getAllAccounts();
  if (accounts.length === 0) {
    return null;
  }

  const account = accounts[0];
  setActiveAccount(account);
  return account;
}
