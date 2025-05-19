const jwt = require('jsonwebtoken');
const logger = require('../utils/logger');

const auth = (req, res, next) => {
  try {
    const token = req.header('Authorization')?.replace('Bearer ', '');
    if (!token) {
      logger.warn('Tentativa de acesso sem token');
      return res.status(401).json({ error: 'Autenticação necessária' });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    logger.error(`Erro de autenticação: ${err.message}`);
    res.status(401).json({ error: 'Token inválido' });
  }
};

module.exports = auth;