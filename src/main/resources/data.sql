insert into setting (key, value)
values
    ('command_prefix', '!'),
    ('bday_announce_channel', '977175565377347635')

on conflict (key) do nothing;