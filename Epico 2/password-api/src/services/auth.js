const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const User = require('../models/user');
const logger = require('../utils/logger');

const register = async (username, password) => {
  try {
    const existingUser = await User.findOne({ username });
    if (existingUser) {
      logger.warn(`Tentativa de registo com username existente: ${username}`);
      throw new Error('Utilizador já existe');
    }

    const user = new User({
      id: uuidv4(),
      username,
      password,
      roles: ['user'],
    });

    await user.save();
    logger.info(`Utilizador registado: ${username}`);
    return { id: user.id, username: user.username };
  } catch (err) {
    logger.error(`Erro ao registar utilizador: ${err.message}`);
    throw err;
  }
};

const login = async (username, password) => {
  try {
    const user = await User.findOne({ username });
    if (!user) {
      logger.warn(`Tentativa de login com username inexistente: ${username}`);
      throw new Error('Credenciais inválidas');
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      logger.warn(`password incorreta para utilizador: ${username}`);
      throw new Error('Credenciais inválidas');
    }

    const token = jwt.sign(
      { id: user.id, username: user.username, roles: user.roles },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    logger.info(`Utilizador logado: ${username}`);
    return { token, user: { id: user.id, username: user.username } };
  } catch (err) {
    logger.error(`Erro ao fazer login: ${err.message}`);
    throw err;
  }
};

module.exports = { register, login };