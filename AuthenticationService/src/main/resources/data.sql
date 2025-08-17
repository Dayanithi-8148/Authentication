INSERT INTO user_permissions (user_id, action, resource, effect) VALUES
('user123', 'read', 'transactions', 'allow'),
('user123', 'write', 'transactions', 'allow'),
('user123', 'delete', 'transactions', 'deny'),
('user123', 'read', 'accounts', 'allow'),
('user456', 'read', 'wallets/*', 'allow'),
('user456', 'write', 'wallets/wallet-789', 'allow'),
('user456', 'read', 'wallets/wallet-789/transactions', 'allow'),
('user789', 'write', 'wallets/*/transactions/*', 'allow'),
('admin789', 'read', '**', 'allow'),
('admin789', 'write', '**', 'allow'),
('admin789', 'delete', '**', 'allow');