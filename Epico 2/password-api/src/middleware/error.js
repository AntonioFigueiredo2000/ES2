const logger = require('../utils/logger');

const errorHandler = (err, req, res, next) => {
  logger.error(`Erro não tratado: ${err.message}`);
  res.status(500).json({ error: 'Erro interno do servidor' });
};

module.exports = errorHandler;