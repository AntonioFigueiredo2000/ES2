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

    if (username.length < 5 || username.length > 20) {
      logger.warn(`Tentativa de registro com username fora dos limites estabelecidos: ${username.length}`);
      return res.status(400).json({ error: 'Username precisa de ter entre 5 e 20 caracteres' });
    }

    if (password.length < 8 || password.length > 32) {
      logger.warn('Tentativa de registro com password fora dos limites estabelecidos');
      return res.status(400).json({ error: 'Password precisa de ter entre 8 e 32 caracteres' });
    }

    const user = await authService.register(username, password);
    res.status(201).json(user);
  } catch (err) {
    logger.warn('Erro ao criar utilizador');
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

      if (name.length < 10 || name.length > 40) {
      logger.warn('Tentativa de registro com nome da aplicaçao fora dos limites estabelecidos');
      return res.status(400).json({ error: 'Nome da aplicaçao precisa de ter entre 10 e 40 caracteres' });
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

    if (password.length < 8 || password.length > 32) {
      logger.warn('Tentativa de atualização da password fora dos limites estabelecidos');
      return res.status(400).json({ error: 'Password precisa de ter entre 8 e 32 caracteres' });
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
      return res.json({ message: 'Password atualizada com sucesso' });
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

    if (password.length < 8 || password.length > 32) {
      logger.warn('Tentativa de atualização da password fora dos limites estabelecidos');
      return res.status(400).json({ error: 'Password precisa de ter entre 8 e 32 caracteres' });
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
      logger.warn(`Password não encontrada para app ${appid}`);
      return res.status(404).json({ error: 'Aplicação sem password defenida' });
    }

    existingPassword.password = password;
    existingPassword.createdBy = req.user.id;
    await existingPassword.save();
    logger.info(`password atualizada para app ${appid} por utilizador ${req.user.id}`);
    res.json({ message: 'Password atualizada com sucesso' });
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
      return res.status(404).json({ error: 'Aplicação não encontrada' });
    }

    const ability = defineAbilitiesFor(req.user, app);
    // if (!ability.can('read', 'Password', { appid })) {
    //   logger.warn(`Acesso negado para Utilizador ${req.user.id} no app ${appid}`);
    //   return res.status(403).json({ error: 'Acesso negado' });
    // }

    const password = await Password.findOne({ appid });
    if (!password) {
      logger.warn(`password não encontrada para app ${appid}`);
      return res.status(404).json({ error: 'Aplicação sem password defenida' });
    }

    logger.info(`password obtida para app ${appid} por utilizador ${req.user.id}`);
    res.json({ password: password.password });
  } catch (err) {
    logger.error(`Erro ao obter password: ${err.message}`);
    res.status(400).json({ error: err.message });
  }
});

// Importar Stub da API externa
const externalApi = require('../../stubs/externalApi');

// Importar Apps
router.post('/import/apps', auth, async (req, res) => {
  try {
    // Obter dados da API externa (usando o stub)
    const externalApps = await externalApi.fetchApps();
    logger.info(`Iniciando importação de ${externalApps.length} apps para o utilizador ${req.user.id}`);

    // Mapear apps externas para o modelo App
    const appsToSave = externalApps.map((externalApp) => ({
      appid: externalApp.externalId, // Usar externalId como appid
      name: externalApp.name,
      owner: req.user.id,
      editors: [],
    }));

    // Inserir ou atualizar apps no banco de dados
    const savedApps = [];
    for (const appData of appsToSave) {
      const existingApp = await App.findOne({ appid: appData.appid });
      if (existingApp) {
        // Atualizar app existente
        existingApp.name = appData.name;
        await existingApp.save();
        logger.info(`App atualizada: ${appData.appid} por utilizador ${req.user.id}`);
      } else {
        // Criar nova app
        const newApp = new App(appData);
        await newApp.save();
        logger.info(`App criada: ${appData.appid} por utilizador ${req.user.id}`);
      }
      savedApps.push({ appid: appData.appid, name: appData.name });
    }

    res.status(201).json({
      message: `Importação concluída: ${savedApps.length} apps processadas`,
      apps: savedApps,
    });
  } catch (err) {
    logger.error(`Erro ao importar apps: ${err.message}`);
    res.status(500).json({ error: 'Erro ao importar apps' });
  }
});

module.exports = router;