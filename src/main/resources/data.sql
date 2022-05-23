insert into setting (key, value)
values
    ('bank_enabled', 'true'),
    ('rob_enabled', 'true'),
    ('work_enabled', 'true'),
    ('slut_enabled', 'true'),
    ('crime_enabled', 'true'),
    ('income_min_ratio', '0.0'),
    ('income_max_ratio', '0.8'),
    ('income_base_rate_max', '1000'),
    ('work_cooldown_ms', '60000'),
    ('rob_cooldown_ms', '60000'),
    ('crime_cooldown_ms', '60000'),
    ('slut_cooldown_ms', '60000'),
    ('chat_income_min', '50'),
    ('chat_income_max', '150'),
    ('roulette_enabled', 'false'),
    ('roulette_timer', '30'),
    ('bday_announce_channel', '977175565377347635')

on conflict (key) do nothing;