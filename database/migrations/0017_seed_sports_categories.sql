-- Migration: 0017_seed_sports_categories.sql
-- Description: Seed sports_categories table with all 19 sport types
-- Created: 2026-02-28

-- ============================================================
-- UP
-- ============================================================

INSERT INTO sports_categories (name, sport_type, description, display_order) VALUES
('Basketball',   'basketball',   'Indoor and outdoor basketball courts', 1),
('Football',     'football',     'Football pitches and training fields', 2),
('Tennis',       'tennis',       'Tennis courts for singles and doubles', 3),
('Paddle',       'paddle',       'Paddle tennis courts', 4),
('Volleyball',   'volleyball',   'Indoor and beach volleyball courts', 5),
('Swimming',     'swimming',     'Swimming pools and aquatic centers', 6),
('Boxing',       'boxing',       'Boxing gyms and training rings', 7),
('MMA',          'mma',          'Mixed martial arts gyms and cages', 8),
('Yoga',         'yoga',         'Yoga studios and wellness centers', 9),
('Pilates',      'pilates',      'Pilates studios and reformer classes', 10),
('CrossFit',     'crossfit',     'CrossFit boxes and functional fitness', 11),
('Running',      'running',      'Running tracks and trail routes', 12),
('Cycling',      'cycling',      'Cycling tracks and indoor spinning', 13),
('Golf',         'golf',         'Golf courses and driving ranges', 14),
('Badminton',    'badminton',    'Badminton courts', 15),
('Table Tennis', 'table_tennis', 'Table tennis facilities', 16),
('Handball',     'handball',     'Handball courts and arenas', 17),
('Baseball',     'baseball',     'Baseball diamonds and batting cages', 18),
('Cricket',      'cricket',      'Cricket grounds and nets', 19);

-- ============================================================
-- DOWN
-- ============================================================

DELETE FROM sports_categories;
