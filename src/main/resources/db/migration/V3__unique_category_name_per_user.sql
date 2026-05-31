CREATE UNIQUE INDEX IF NOT EXISTS uk_categories_user_name_lower_trim
    ON dbo.categories (user_id, lower(btrim(name)));
