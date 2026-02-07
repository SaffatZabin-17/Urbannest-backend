-- Adding updated_at field to media_assets, since they can be edited and new captions can be added later

ALTER TABLE "media_assets" ADD COLUMN "updated_at" timestamptz NOT NULL