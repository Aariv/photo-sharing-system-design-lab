-- =====================================================
-- Add like_count column to posts
-- =====================================================

ALTER TABLE posts
    ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0;

-- =====================================================
-- Backfill existing counts
-- =====================================================

UPDATE posts p
SET like_count = (
    SELECT COUNT(*)
    FROM likes l
    WHERE l.post_id = p.id
);

-- =====================================================
-- Optional comment
-- =====================================================

COMMENT ON COLUMN posts.like_count IS
'Denormalized like counter. Maintained by LikeService.';