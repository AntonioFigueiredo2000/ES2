const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const logger = require('./utils/logger');
const errorHandler = require('./middleware/error');
const apiRoutes = require('./routes/api');
const swaggerUi = require('swagger-ui-express'); // Adicionar Swagger UI
const yaml = require('js-yaml'); // Adicionar YAML parser
const fs = require('fs'); // Para ler o arquivo swagger.yaml

dotenv.config();

const app = express();

// Middleware
app.use(express.json());

// Carregar a especificação Swagger
const swaggerDocument = yaml.load(fs.readFileSync('./src/swagger.yaml', 'utf8'));

// Rota para a documentação Swagger
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

// Rotas
app.use('/api', apiRoutes);

// Health check
app.get('/', (req, res) => {
  logger.info('Rota health check acessada');
  res.json({ message: 'API a correr!' });
});

// Middleware de erro
app.use(errorHandler);

// Conexão com MongoDB Atlas
mongoose
  .connect(process.env.MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  })
  .then(() => {
    logger.info('Conectado ao MongoDB Atlas');
  })
  .catch((err) => {
    logger.error(`Erro ao conectar ao MongoDB: ${err.message}`);
    process.exit(1);
  });

// Iniciar servidor
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  logger.info(`Servidor a correr na porta ${PORT}`);
});