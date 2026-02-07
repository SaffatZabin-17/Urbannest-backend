-- Adding ON UPDATE /ON DELETE constraints to the database schema


-- Removing user_id foreign keys

ALTER TABLE "listings" DROP CONSTRAINT "listings_user_id_fkey";

ALTER TABLE "media_assets" DROP CONSTRAINT "media_assets_owner_user_id_fkey";

ALTER TABLE "blog_votes" DROP CONSTRAINT "blog_votes_user_id_fkey";

ALTER TABLE "comment_votes" DROP CONSTRAINT "comment_votes_user_id_fkey";

ALTER TABLE "favorite_listings" DROP CONSTRAINT "favorite_listings_user_id_fkey";

ALTER TABLE "saved_listings" DROP CONSTRAINT "saved_listings_user_id_fkey";

ALTER TABLE "favorite_blogs" DROP CONSTRAINT "favorite_blogs_user_id_fkey";

ALTER TABLE "saved_blogs" DROP CONSTRAINT "saved_blogs_user_id_fkey";

ALTER TABLE "notifications" DROP CONSTRAINT "notifications_recipient_user_id_fkey";

ALTER TABLE "blogs" DROP CONSTRAINT "blogs_author_id_fkey";

ALTER TABLE "comments" DROP CONSTRAINT "comments_author_id_fkey";

-- Removing listing_id foreign keys

ALTER TABLE "listing_details" DROP CONSTRAINT "listing_details_listing_id_fkey";

ALTER TABLE "listing_locations" DROP CONSTRAINT "listing_locations_listing_id_fkey";

ALTER TABLE "listing_price_history" DROP CONSTRAINT "listing_price_history_listing_id_fkey";

ALTER TABLE "listing_counters" DROP CONSTRAINT "listing_counters_listing_id_fkey";

ALTER TABLE "listing_media" DROP CONSTRAINT "listing_media_listing_id_fkey";

ALTER TABLE "favorite_listings" DROP CONSTRAINT "favorite_listings_listing_id_fkey";

ALTER TABLE "saved_listings" DROP CONSTRAINT "saved_listings_listing_id_fkey";

-- Removing media_id foreign keys

ALTER TABLE "listing_media" DROP CONSTRAINT "listing_media_media_id_fkey";

ALTER TABLE "blog_media" DROP CONSTRAINT "blog_media_media_id_fkey";

-- Removing blog_id foreign keys

ALTER TABLE "blog_media" DROP CONSTRAINT "blog_media_blog_id_fkey";

ALTER TABLE "blog_votes" DROP CONSTRAINT "blog_votes_blog_id_fkey";

ALTER TABLE "comments" DROP CONSTRAINT "comments_blog_id_fkey";

ALTER TABLE "favorite_blogs" DROP CONSTRAINT "favorite_blogs_blog_id_fkey";

ALTER TABLE "saved_blogs" DROP CONSTRAINT "saved_blogs_blog_id_fkey";

-- Removing comment_id foreign keys

ALTER TABLE "comments" DROP CONSTRAINT "comments_parent_comment_id_fkey";

ALTER TABLE "comment_votes" DROP CONSTRAINT "comment_votes_comment_id_fkey";


-- Re-adding foreign keys with ON UPDATE CASCADE ON DELETE CASCADE

-- user_id foreign keys

ALTER TABLE "listings" ADD FOREIGN KEY ("user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "media_assets" ADD FOREIGN KEY ("owner_user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "blog_votes" ADD FOREIGN KEY ("user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "comment_votes" ADD FOREIGN KEY ("user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "favorite_listings" ADD FOREIGN KEY ("user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "saved_listings" ADD FOREIGN KEY ("user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "favorite_blogs" ADD FOREIGN KEY ("user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "saved_blogs" ADD FOREIGN KEY ("user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "notifications" ADD FOREIGN KEY ("recipient_user_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "blogs" ADD FOREIGN KEY ("author_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "comments" ADD FOREIGN KEY ("author_id")
  REFERENCES "users" ("user_id") ON UPDATE CASCADE ON DELETE CASCADE;

-- listing_id foreign keys

ALTER TABLE "listing_details" ADD FOREIGN KEY ("listing_id")
  REFERENCES "listings" ("listing_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "listing_locations" ADD FOREIGN KEY ("listing_id")
  REFERENCES "listings" ("listing_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "listing_price_history" ADD FOREIGN KEY ("listing_id")
  REFERENCES "listings" ("listing_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "listing_counters" ADD FOREIGN KEY ("listing_id")
  REFERENCES "listings" ("listing_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "listing_media" ADD FOREIGN KEY ("listing_id")
  REFERENCES "listings" ("listing_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "favorite_listings" ADD FOREIGN KEY ("listing_id")
  REFERENCES "listings" ("listing_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "saved_listings" ADD FOREIGN KEY ("listing_id")
  REFERENCES "listings" ("listing_id") ON UPDATE CASCADE ON DELETE CASCADE;

-- media_id foreign keys

ALTER TABLE "listing_media" ADD FOREIGN KEY ("media_id")
  REFERENCES "media_assets" ("media_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "blog_media" ADD FOREIGN KEY ("media_id")
  REFERENCES "media_assets" ("media_id") ON UPDATE CASCADE ON DELETE CASCADE;

-- blog_id foreign keys

ALTER TABLE "blog_media" ADD FOREIGN KEY ("blog_id")
  REFERENCES "blogs" ("blog_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "blog_votes" ADD FOREIGN KEY ("blog_id")
  REFERENCES "blogs" ("blog_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "comments" ADD FOREIGN KEY ("blog_id")
  REFERENCES "blogs" ("blog_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "favorite_blogs" ADD FOREIGN KEY ("blog_id")
  REFERENCES "blogs" ("blog_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "saved_blogs" ADD FOREIGN KEY ("blog_id")
  REFERENCES "blogs" ("blog_id") ON UPDATE CASCADE ON DELETE CASCADE;

-- comment_id foreign keys

ALTER TABLE "comments" ADD FOREIGN KEY ("parent_comment_id")
  REFERENCES "comments" ("comment_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "comment_votes" ADD FOREIGN KEY ("comment_id")
  REFERENCES "comments" ("comment_id") ON UPDATE CASCADE ON DELETE CASCADE;
