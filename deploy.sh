#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")"

if [[ ! -f .env ]]; then
  echo "Missing .env. Run: cp .env.example .env"
  exit 1
fi
if grep -Eq '^[A-Z_]+=.*CHANGE_ME' .env; then
  echo "Replace every CHANGE_ME value in .env before deployment."
  exit 1
fi
for required_key in MYSQL_PASSWORD MYSQL_ROOT_PASSWORD REDIS_PASSWORD JWT_SECRET ADMIN_BOOTSTRAP_USERNAME ADMIN_BOOTSTRAP_EMAIL ADMIN_BOOTSTRAP_PASSWORD MAIL_USERNAME MAIL_PASSWORD; do
  required_value="$(sed -n "s/^${required_key}=//p" .env | tail -1)"
  if [[ -z "$required_value" ]]; then
    echo "Missing required value: $required_key"
    exit 1
  fi
done

mkdir -p runtime/files/bussiness runtime/files/video logs
awk '!/^INSERT INTO `(user|user_achievement|user_backpack|user_checkin|user_collect|user_quiz_record|user_quiz_stats|user_quize_mistakes|quiz_question|community_post|post_collect|post_comment|post_like|post_report|badge|site_visit|sys_file_info)`/' \
  heritage_db.sql > runtime/heritage-public.sql
for asset_dir in extracourse illness inheritor jingluo media shuxue skills start zhenfa; do
  if [[ -d "springboot/files/bussiness/$asset_dir" && ! -e "runtime/files/bussiness/$asset_dir" ]]; then
    cp -a "springboot/files/bussiness/$asset_dir" "runtime/files/bussiness/$asset_dir"
  fi
done
if [[ -d springboot/files/video && -z "$(find runtime/files/video -mindepth 1 -print -quit)" ]]; then
  cp -a springboot/files/video/. runtime/files/video/
fi
app_uid="$(sed -n 's/^APP_UID=//p' .env | tail -1)"
app_gid="$(sed -n 's/^APP_GID=//p' .env | tail -1)"
app_uid="${app_uid:-1000}"
app_gid="${app_gid:-1000}"
if [[ ! "$app_uid" =~ ^[0-9]+$ || ! "$app_gid" =~ ^[0-9]+$ ]]; then
  echo "APP_UID and APP_GID must be numeric."
  exit 1
fi
chown -R "$app_uid:$app_gid" runtime/files logs
docker compose config --quiet

# Start only infrastructure first. A fresh database runs the two init scripts and
# receives the marker below. An older volume without the marker must be reviewed
# manually; continuing automatically could expose demo/clinical data or destroy
# legitimate production users during cleanup.
docker compose up -d --wait mysql redis
if ! docker compose exec -T mysql sh -eu -c '
  result=$(mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -Nse \
    "SELECT COUNT(*) FROM app_migration WHERE migration_key = 0x70726f64756374696f6e5f736565645f73616e6974697a65645f7631")
  [ "$result" = "1" ]
'; then
  echo "Database safety marker is missing. Deployment stopped before the app was started."
  echo "Back up and audit this existing database volume; do not run the destructive cleanup against production users."
  exit 1
fi

docker compose up -d --build
docker compose ps
