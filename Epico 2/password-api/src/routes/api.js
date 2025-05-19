const express = require('express');
const router = express.Router();
const authService = require('../services/auth');
const { defineAbilitiesFor } = require('../services/rebac');
const auth = require('../middleware/auth');
const App = require('../models/app');
const Password = require('../models/password');
const logger = require('../utils/logger');

// Registo
router.post('/register', async (req, res) => {
  try {
    const { username, password } = req.body;
    if (!username || !password) {
      logger.warn('Tentativa de registro sem username ou password');
      return res.status(400).json({ error: 'Username e password são obrigatórios' });
    }

    const user = await authService.register(username, password);
    res.status(201).json(user);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Login
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    if (!username || !password) {
      logger.warn('Tentativa de login sem username ou password');
      return res.status(400).json({ error: 'Username e password são obrigatórios' });
    }

    const result = await authService.login(username, password);
    res.json(result);
  } catch (err) {
    res.status(401).json({ error: err.message });
  }
});

// Criar App
router.post('/app', auth, async (req, res) => {
  try {
    const { name } = req.body;
    if (!name) {
      logger.warn('Tentativa de criar app sem nome');
      return res.status(400).json({ error: 'Nome do app é obrigatório' });
    }

    const app = new App({
      name,
      owner: req.user.id,
      editors: [],
    });

    await app.save();
    logger.info(`App criado: ${app.appid} por utilizador ${req.user.id}`);
    res.status(201).json({ appid: app.appid, name: app.name });
  } catch (err) {
    logger.error(`Erro ao criar app: ${err.message}`);
    res.status(400).json({ error: err.message });
  }
});

// Listar Apps
router.get('/apps', auth, async (req, res) => {
  try {
    const apps = await App.find({ $or: [{ owner: req.user.id }, { editors: req.user.id }] });
    logger.info(`Listagem de apps para utilizador ${req.user.id}`);
    res.json(apps);
  } catch (err) {
    logger.error(`Erro ao listar apps: ${err.message}`);
    res.status(500).json({ error: err.message });
  }
});

// Criar/Atualizar password
router.post('/password/:appid', auth, async (req, res) => {
  try {
    const { appid } = req.params;
    const { password } = req.body;

    if (!password) {
      logger.warn(`Tentativa de criar/atualizar password sem password para app ${appid}`);
      return res.status(400).json({ error: 'Password é obrigatório' });
    }

    const app = await App.findOne({ appid });
    if (!app) {
      logger.warn(`App não encontrado: ${appid}`);
      return res.status(404).json({ error: 'App não encontrado' });
    }

    const ability = defineAbilitiesFor(req.user, app);
    console.log(ability);
    // if (!ability.can('manage', 'Password', { appid }) && !ability.can('update', 'Password', { appid })) {
    //   logger.warn(`Acesso negado para Utilizador ${req.user.id} no app ${appid}`);
    //   return res.status(403).json({ error: 'Acesso negado' });
    // }
    console.log({
      appid,
      password,
      createdBy: req.user.id + "",
    });
    let existingPassword = await Password.findOne({ appid });
    if (existingPassword) {
      existingPassword.password = password;
      existingPassword.createdBy = req.user.id;
      await existingPassword.save();
      logger.info(`password atualizada para app ${appid} por utilizador ${req.user.id}`);
      return res.json({ message: 'password atualizada' });
    }

    const newPassword = new Password({
      appid,
      password,
      createdBy: req.user.id + "",
    });
    
    await newPassword.save();
    logger.info(`password criada para app ${appid} por utilizador ${req.user.id}`);
    res.status(201).json({ message: 'password criada' });
  } catch (err) {
    logger.error(`Erro ao criar/atualizar password: ${err.message}`);
    res.status(400).json({ error: err.message });
  }
});

// Atualizar password (PUT)
router.put('/password/:appid', auth, async (req, res) => {
  try {
    const { appid } = req.params;
    const { password } = req.body;

    if (!password) {
      logger.warn(`Tentativa de atualizar password sem password para app ${appid}`);
      return res.status(400).json({ error: 'Password é obrigatório' });
    }

    const app = await App.findOne({ appid });
    if (!app) {
      logger.warn(`App não encontrado: ${appid}`);
      return res.status(404).json({ error: 'App não encontrado' });
    }

     const ability = defineAbilitiesFor(req.user, app);
    // if (!ability.can('update', 'Password', { appid })) {
    //   logger.warn(`Acesso negado para Utilizador ${req.user.id} no app ${appid}`);
    //   return res.status(403).json({ error: 'Acesso negado' });
    // }

    const existingPassword = await Password.findOne({ appid });
    if (!existingPassword) {
      logger.warn(`password não encontrada para app ${appid}`);
      return res.status(404).json({ error: 'password não encontrada' });
    }

    existingPassword.password = password;
    existingPassword.createdBy = req.user.id;
    await existingPassword.save();
    logger.info(`password atualizada para app ${appid} por utilizador ${req.user.id}`);
    res.json({ message: 'password atualizada' });
  } catch (err) {
    logger.error(`Erro ao atualizar password: ${err.message}`);
    res.status(400).json({ error: err.message });
  }
});

// Obter password
router.get('/password/:appid', auth, async (req, res) => {
  try {
    const { appid } = req.params;

    const app = await App.findOne({ appid });
    if (!app) {
      logger.warn(`App não encontrado: ${appid}`);
      return res.status(404).json({ error: 'App não encontrado' });
    }

    const ability = defineAbilitiesFor(req.user, app);
    // if (!ability.can('read', 'Password', { appid })) {
    //   logger.warn(`Acesso negado para Utilizador ${req.user.id} no app ${appid}`);
    //   return res.status(403).json({ error: 'Acesso negado' });
    // }

    const password = await Password.findOne({ appid });
    if (!password) {
      logger.warn(`password não encontrada para app ${appid}`);
      return res.status(404).json({ error: 'password não encontrada' });
    }

    logger.info(`password obtida para app ${appid} por utilizador ${req.user.id}`);
    res.json({ password: password.password });
  } catch (err) {
    logger.error(`Erro ao obter password: ${err.message}`);
    res.status(400).json({ error: err.message });
  }
});

module.exports = router;