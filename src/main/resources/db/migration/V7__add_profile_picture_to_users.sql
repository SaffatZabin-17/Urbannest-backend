-- Adding the column profile_picture to simulate s3 bucket functionalities, cause I don't have enough time to implement the whole thing end-to-end

ALTER TABLE "users" ADD COLUMN "profile_picture_url" varchar;