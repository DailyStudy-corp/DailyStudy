set -e

echo "Criando usuário de aplicação '$MONGO_APP_USER' no banco '$MONGO_INITDB_DATABASE'..."

mongosh --quiet <<MONGO_EOF
db = db.getSiblingDB("$MONGO_INITDB_DATABASE");
db.createUser({
  user: "$MONGO_APP_USER",
  pwd: "$MONGO_APP_PASSWORD",
  roles: [ { role: "readWrite", db: "$MONGO_INITDB_DATABASE" } ]
});
MONGO_EOF

echo "Usuário '$MONGO_APP_USER' criado com sucesso."