-- Migration: 0040_seed_achievements.sql
-- Description: Seed achievements table with the initial set of booking, match, social, and general achievements
-- Created: 2026-03-18

-- ============================================================
-- UP
-- ============================================================

INSERT INTO achievements
    (name, description, icon, category, xp_reward, criteria_type, criteria_value)
VALUES

    -- --------------------------------------------------------
    -- Booking achievements
    -- --------------------------------------------------------
    (
        'First Booking',
        'Complete your first venue or coach booking',
        'sports',
        'booking',
        50,
        'booking_count',
        1
    ),
    (
        'Regular Player',
        'Complete 10 bookings',
        'repeat',
        'booking',
        200,
        'booking_count',
        10
    ),
    (
        'Court Regular',
        'Complete 25 bookings',
        'emoji_events',
        'booking',
        500,
        'booking_count',
        25
    ),
    (
        'Venue Legend',
        'Complete 50 bookings',
        'military_tech',
        'booking',
        1000,
        'booking_count',
        50
    ),

    -- --------------------------------------------------------
    -- Match achievements
    -- --------------------------------------------------------
    (
        'First Match',
        'Play your first match',
        'sports_score',
        'match',
        50,
        'match_count',
        1
    ),
    (
        'Team Player',
        'Play 10 matches',
        'groups',
        'match',
        200,
        'match_count',
        10
    ),
    (
        'Match Master',
        'Play 25 matches',
        'workspace_premium',
        'match',
        500,
        'match_count',
        25
    ),

    -- --------------------------------------------------------
    -- Social achievements
    -- --------------------------------------------------------
    (
        'First Review',
        'Write your first review',
        'rate_review',
        'social',
        30,
        'review_count',
        1
    ),
    (
        'Critic',
        'Write 10 reviews',
        'reviews',
        'social',
        150,
        'review_count',
        10
    ),
    (
        'Social Butterfly',
        'Add 5 friends',
        'people',
        'social',
        100,
        'friend_count',
        5
    ),

    -- --------------------------------------------------------
    -- General achievements
    -- --------------------------------------------------------
    (
        'Sport Explorer',
        'Book venues or coaches in 3 different sports',
        'explore',
        'general',
        150,
        'sport_variety',
        3
    ),
    (
        'All-Rounder',
        'Book venues or coaches in 5 different sports',
        'stars',
        'general',
        300,
        'sport_variety',
        5
    ),
    (
        'Early Bird',
        'Complete a booking for a 9 AM time slot',
        'wb_sunny',
        'general',
        75,
        'early_bird',
        1
    ),
    (
        'Night Owl',
        'Complete a booking for a 9 PM time slot',
        'nightlight',
        'general',
        75,
        'night_owl',
        1
    );

-- ============================================================
-- DOWN
-- ============================================================

DELETE FROM achievements
WHERE name IN (
    'First Booking',
    'Regular Player',
    'Court Regular',
    'Venue Legend',
    'First Match',
    'Team Player',
    'Match Master',
    'First Review',
    'Critic',
    'Social Butterfly',
    'Sport Explorer',
    'All-Rounder',
    'Early Bird',
    'Night Owl'
);
