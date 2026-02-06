CREATE TYPE "property_type" AS ENUM (
  'house',
  'apartment',
  'studio',
  'condo',
  'penthouse'
);

CREATE TYPE "property_status" AS ENUM (
  'draft',
  'published',
  'pending',
  'sold',
  'archived'
);

CREATE TYPE "listing_condition" AS ENUM (
  'new',
  'used',
  'renovated',
  'under_construction'
);

CREATE TYPE "facing_direction" AS ENUM (
  'north',
  'south',
  'east',
  'west'
);

CREATE TYPE "media_content_type" AS ENUM (
  'img',
  'video'
);

CREATE TYPE "blog_status" AS ENUM (
  'draft',
  'published',
  'hidden'
);

CREATE TYPE "notification_type" AS ENUM (
  'NEW_MESSAGE',
  'LISTING_APPROVED',
  'NEW_COMMENT',
  'PRICE_DROP',
  'SYSTEM'
);

CREATE TYPE "notification_entity_type" AS ENUM (
  'LISTING',
  'BLOG',
  'COMMENT',
  'MESSAGE',
  'DEAL',
  'NONE'
);

CREATE TABLE "users" (
  "user_id" uuid PRIMARY KEY NOT NULL,
  "cognito_sub" text UNIQUE NOT NULL,
  "name" varchar NOT NULL,
  "email" varchar UNIQUE NOT NULL,
  "phone" varchar UNIQUE,
  "nid_hash" text UNIQUE NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "deleted_at" timestamptz,
  "role_name" varchar NOT NULL
);

CREATE TABLE "listings" (
  "listing_id" uuid PRIMARY KEY NOT NULL,
  "user_id" uuid NOT NULL,
  "property_type" property_type NOT NULL,
  "property_status" property_status NOT NULL,
  "title" text NOT NULL,
  "description" text,
  "pricing" numeric(15,2) NOT NULL,
  "created_at" timestamptz NOT NULL,
  "published_at" timestamptz,
  "updated_at" timestamptz NOT NULL,
  "deleted_at" timestamptz
);

CREATE TABLE "listing_details" (
  "listing_id" uuid PRIMARY KEY NOT NULL,
  "year_built" int NOT NULL,
  "listing_condition" listing_condition NOT NULL,
  "facing_direction" facing_direction,
  "bedrooms_count" integer NOT NULL,
  "bathrooms_count" integer NOT NULL,
  "balconies_count" integer NOT NULL,
  "floor_level" integer,
  "furnished" bool,
  "parking_area" integer,
  "pet_friendly" bool,
  "lot_area" integer,
  "living_area" integer NOT NULL
);

CREATE TABLE "listing_locations" (
  "listing_id" uuid PRIMARY KEY NOT NULL,
  "address_line" text NOT NULL,
  "area" text NOT NULL,
  "district" text NOT NULL,
  "zip_code" varchar(10) NOT NULL,
  "latitude" decimal(9,6) NOT NULL,
  "longitude" decimal(9,6) NOT NULL
);

CREATE TABLE "listing_price_history" (
  "id" uuid PRIMARY KEY NOT NULL,
  "listing_id" uuid NOT NULL,
  "old_price" numeric(15,2) NOT NULL,
  "new_price" numeric(15,2) NOT NULL,
  "changed_at" timestamptz NOT NULL
);

CREATE TABLE "listing_counters" (
  "listing_id" uuid PRIMARY KEY NOT NULL,
  "view_count" integer NOT NULL DEFAULT 0,
  "favorite_count" integer NOT NULL DEFAULT 0,
  "save_count" integer NOT NULL DEFAULT 0
);

CREATE TABLE "media_assets" (
  "media_id" uuid PRIMARY KEY NOT NULL,
  "owner_user_id" uuid NOT NULL,
  "s3_location" varchar NOT NULL,
  "content_type" media_content_type NOT NULL,
  "byte_size" bigint NOT NULL,
  "caption" text,
  "created_at" timestamptz NOT NULL,
  "deleted_at" timestamptz,
  "metadata" jsonb
);

CREATE TABLE "listing_media" (
  "media_id" uuid NOT NULL,
  "listing_id" uuid NOT NULL,
  "sort_order" integer NOT NULL,
  PRIMARY KEY ("listing_id", "media_id")
);

CREATE TABLE "blogs" (
  "blog_id" uuid PRIMARY KEY NOT NULL,
  "author_id" uuid NOT NULL,
  "title" text NOT NULL,
  "content" jsonb NOT NULL,
  "status" blog_status NOT NULL,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "deleted_at" timestamptz
);

CREATE TABLE "blog_media" (
  "blog_id" uuid NOT NULL,
  "media_id" uuid NOT NULL,
  PRIMARY KEY ("blog_id", "media_id")
);

CREATE TABLE "blog_votes" (
  "blog_id" uuid NOT NULL,
  "user_id" uuid NOT NULL,
  "vote_value" int NOT NULL,
  "created_at" timestamptz,
  PRIMARY KEY ("user_id", "blog_id")
);

CREATE TABLE "comments" (
  "comment_id" uuid PRIMARY KEY NOT NULL,
  "author_id" uuid NOT NULL,
  "blog_id" uuid NOT NULL,
  "body" jsonb NOT NULL,
  "parent_comment_id" uuid,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz NOT NULL,
  "deleted_at" timestamptz
);

CREATE TABLE "comment_votes" (
  "comment_id" uuid NOT NULL,
  "user_id" uuid NOT NULL,
  "vote_value" int NOT NULL,
  "created_at" timestamptz,
  PRIMARY KEY ("user_id", "comment_id")
);

CREATE TABLE "favorite_listings" (
  "user_id" uuid NOT NULL,
  "listing_id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  PRIMARY KEY ("user_id", "listing_id")
);

CREATE TABLE "saved_listings" (
  "user_id" uuid NOT NULL,
  "listing_id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  PRIMARY KEY ("user_id", "listing_id")
);

CREATE TABLE "favorite_blogs" (
  "user_id" uuid NOT NULL,
  "blog_id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  PRIMARY KEY ("user_id", "blog_id")
);

CREATE TABLE "saved_blogs" (
  "user_id" uuid NOT NULL,
  "blog_id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  PRIMARY KEY ("user_id", "blog_id")
);

CREATE TABLE "notifications" (
  "notification_id" uuid PRIMARY KEY,
  "recipient_user_id" uuid NOT NULL,
  "type" notification_type NOT NULL,
  "payload" jsonb NOT NULL,
  "entity_type" notification_entity_type NOT NULL,
  "entity_id" uuid,
  "created_at" timestamptz NOT NULL,
  "read_at" timestamptz,
  "deleted_at" timestamptz
);

CREATE UNIQUE INDEX ON "listing_media" ("media_id");

CREATE UNIQUE INDEX ON "blog_media" ("media_id");

CREATE INDEX ON "favorite_listings" ("listing_id");

CREATE INDEX ON "saved_listings" ("listing_id");

CREATE INDEX ON "favorite_blogs" ("blog_id");

CREATE INDEX ON "saved_blogs" ("blog_id");

CREATE INDEX ON "notifications" ("recipient_user_id", "created_at");

CREATE INDEX ON "notifications" ("recipient_user_id", "read_at");

CREATE INDEX ON "notifications" ("entity_type", "entity_id");

ALTER TABLE "listings" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "listing_details" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("listing_id");

ALTER TABLE "listing_locations" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("listing_id");

ALTER TABLE "listing_price_history" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("listing_id");

ALTER TABLE "listing_counters" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("listing_id");

ALTER TABLE "media_assets" ADD FOREIGN KEY ("owner_user_id") REFERENCES "users" ("user_id");

ALTER TABLE "listing_media" ADD FOREIGN KEY ("media_id") REFERENCES "media_assets" ("media_id");

ALTER TABLE "listing_media" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("listing_id");

ALTER TABLE "blogs" ADD FOREIGN KEY ("author_id") REFERENCES "users" ("user_id");

ALTER TABLE "blog_media" ADD FOREIGN KEY ("blog_id") REFERENCES "blogs" ("blog_id");

ALTER TABLE "blog_media" ADD FOREIGN KEY ("media_id") REFERENCES "media_assets" ("media_id");

ALTER TABLE "comments" ADD FOREIGN KEY ("author_id") REFERENCES "users" ("user_id");

ALTER TABLE "comments" ADD FOREIGN KEY ("blog_id") REFERENCES "blogs" ("blog_id");

ALTER TABLE "blog_votes" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "blog_votes" ADD FOREIGN KEY ("blog_id") REFERENCES "blogs" ("blog_id");

ALTER TABLE "comments" ADD FOREIGN KEY ("parent_comment_id") REFERENCES "comments" ("comment_id");

ALTER TABLE "comment_votes" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "comment_votes" ADD FOREIGN KEY ("comment_id") REFERENCES "comments" ("comment_id");

ALTER TABLE "favorite_listings" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "favorite_listings" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("listing_id");

ALTER TABLE "saved_listings" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "saved_listings" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("listing_id");

ALTER TABLE "favorite_blogs" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "favorite_blogs" ADD FOREIGN KEY ("blog_id") REFERENCES "blogs" ("blog_id");

ALTER TABLE "saved_blogs" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "saved_blogs" ADD FOREIGN KEY ("blog_id") REFERENCES "blogs" ("blog_id");

ALTER TABLE "notifications" ADD FOREIGN KEY ("recipient_user_id") REFERENCES "users" ("user_id");
