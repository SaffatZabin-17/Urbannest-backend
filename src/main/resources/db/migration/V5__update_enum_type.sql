-- Renaming the property_status enum value new to brand_new, because new is a reserved keyword in Java

ALTER TYPE listing_condition RENAME VALUE 'new' TO 'brand_new';