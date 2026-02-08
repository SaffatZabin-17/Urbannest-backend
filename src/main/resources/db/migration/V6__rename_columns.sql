-- Renaming the users.cognito_sub column to firebase_uid and media_asset.s3_location column to storage_url
-- Because AWS services are shit and buggy and SMS verification doesn't fucking work

ALTER TABLE "users" RENAME COLUMN "cognito_sub" TO "firebase_uid"

ALTER TABLE "media_asset" RENAME COLUMN "s3_location" TO "storage_url"