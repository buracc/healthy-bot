insert into setting (key, value)
values
    ('bank_enabled', 'true'),
    ('rob_enabled', 'true'),
    ('work_enabled', 'true'),
    ('slut_enabled', 'true'),
    ('crime_enabled', 'true'),
    ('income_min_ratio', '0.0'),
    ('income_max_ratio', '0.8'),
    ('income_base_rate', '1000'),
    ('work_cooldown_ms', '60000'),
    ('rob_cooldown_ms', '60000'),
    ('crime_cooldown_ms', '60000'),
    ('slut_cooldown_ms', '60000')

on conflict (key) do nothing;