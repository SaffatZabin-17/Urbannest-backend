-- Changing from AWS Cognito to Google Firebase for authentication. Updating users table

ALTER TABLE "users" RENAME COLUMN "cognito_sub" TO "firebase_uid";