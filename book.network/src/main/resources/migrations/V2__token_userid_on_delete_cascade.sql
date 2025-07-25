-- Migration: Set ON DELETE CASCADE for token.userId foreign key
-- This migration will allow deleting a user and automatically delete all their tokens.

ALTER TABLE token
DROP CONSTRAINT IF EXISTS fk7kwip70ekiv16e4euwcp2usfj;

ALTER TABLE token
ADD CONSTRAINT fk7kwip70ekiv16e4euwcp2usfj
FOREIGN KEY (userId) REFERENCES app_user(id) ON DELETE CASCADE;
