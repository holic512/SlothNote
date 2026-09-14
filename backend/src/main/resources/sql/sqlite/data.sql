INSERT INTO system_ai_config
    (id, provider_name, base_url, api_key, model, temperature, max_tokens, planner_temperature, planner_max_tokens, enabled)
VALUES
    (1, 'openai-compatible', NULL, NULL, NULL, 0.7, 4096, 0.1, 256, 0)
ON CONFLICT (id) DO NOTHING@@

INSERT INTO system_mail_config
    (id, host, port, username, password, protocol, default_encoding, from_address, from_name, smtp_auth, ssl_enable, starttls_enable, starttls_required, enabled)
VALUES
    (1, NULL, 465, NULL, NULL, 'smtps', 'UTF-8', NULL, NULL, 1, 1, 1, 1, 0)
ON CONFLICT (id) DO NOTHING@@
