alter table videos
add column created_at timestamp default current_timestamp,
add column updated_at timestamp default current_timestamp on update current_timestamp;

create index idx_created_id on videos (created_at desc , id desc)