-- Currently two media types are defined: 1. img and 2. video
-- This is too rigid, hence changing the media types to mime type to have a universal type
-- This would require removing the MediaContentType enum

-- Convert enum column to varchar
ALTER TABLE media_assets
ALTER COLUMN content_type
      TYPE VARCHAR(100)
      USING content_type::text;

-- Drop the enum type
DROP TYPE IF EXISTS media_content_type;