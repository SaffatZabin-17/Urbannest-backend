-- Adding a default value to created_at and updated_at columns

ALTER TABLE "users" ALTER COLUMN "created_at" SET DEFAULT now();
ALTER TABLE "users" ALTER COLUMN "updated_at" SET DEFAULT now();

ALTER TABLE "listings" ALTER COLUMN "created_at" SET DEFAULT now();
ALTER TABLE "listings" ALTER COLUMN "updated_at" SET DEFAULT now();

ALTER TABLE "listing_price_history" ALTER COLUMN "changed_at" SET DEFAULT now();

ALTER TABLE "media_assets" ALTER COLUMN "created_at" SET DEFAULT now();
ALTER TABLE "media_assets" ALTER COLUMN "updated_at" SET DEFAULT now();

ALTER TABLE "blogs" ALTER COLUMN "created_at" SET DEFAULT now();
ALTER TABLE "blogs" ALTER COLUMN "updated_at" SET DEFAULT now();

ALTER TABLE "comments" ALTER COLUMN "created_at" SET DEFAULT now();
ALTER TABLE "comments" ALTER COLUMN "updated_at" SET DEFAULT now();

ALTER TABLE "favorite_listings" ALTER COLUMN "created_at" SET DEFAULT now();

ALTER TABLE "saved_listings" ALTER COLUMN "created_at" SET DEFAULT now();

ALTER TABLE "favorite_blogs" ALTER COLUMN "created_at" SET DEFAULT now();

ALTER TABLE "saved_blogs" ALTER COLUMN "created_at" SET DEFAULT now();

ALTER TABLE "notifications" ALTER COLUMN "created_at" SET DEFAULT now();