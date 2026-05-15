-- UP
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'match_completed';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'review_reminder';

-- DOWN
-- PostgreSQL cannot remove enum values; manual migration required.
