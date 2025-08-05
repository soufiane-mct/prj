-- Migration: Add latitude and longitude columns to book table
ALTER TABLE book ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE book ADD COLUMN longitude DOUBLE PRECISION;
