INSERT INTO user_schema.users (email, password, first_name, last_name, role)
VALUES
  ('admin@agile.local', '$2a$12$t80acSX5P5cy3f97.Y3lQuILkGariOmfRRB2uEgAf2dFTpsRVstJS', 'Amina', 'Haddad', CAST('ADMIN' AS user_schema.role_enum)),
  ('po@agile.local', '$2a$12$t80acSX5P5cy3f97.Y3lQuILkGariOmfRRB2uEgAf2dFTpsRVstJS', 'Youssef', 'Bennani', CAST('PO' AS user_schema.role_enum)),
  ('sm@agile.local', '$2a$12$t80acSX5P5cy3f97.Y3lQuILkGariOmfRRB2uEgAf2dFTpsRVstJS', 'Salma', 'Khalid', CAST('SM' AS user_schema.role_enum)),
  ('dev1@agile.local', '$2a$12$t80acSX5P5cy3f97.Y3lQuILkGariOmfRRB2uEgAf2dFTpsRVstJS', 'Omar', 'Fassi', CAST('DEV' AS user_schema.role_enum)),
  ('mgr@agile.local', '$2a$12$t80acSX5P5cy3f97.Y3lQuILkGariOmfRRB2uEgAf2dFTpsRVstJS', 'Nadia', 'El Amrani', CAST('MA' AS user_schema.role_enum));
