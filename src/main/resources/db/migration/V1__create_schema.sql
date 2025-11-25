-- V1__create_schema.sql
-- Schema creation for ms-checkout microservice

-- Enable UUID extension if not already enabled (requires superuser or extension already available)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the checkout schema if it doesn't exist
-- Note: Flyway is configured to use this schema, so this ensures it exists
CREATE SCHEMA IF NOT EXISTS checkout;
