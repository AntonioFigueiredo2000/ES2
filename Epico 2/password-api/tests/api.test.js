const request = require('supertest');
const express = require('express');
const router = require('../src/routes/api');
const logger = require('../src/utils/logger');
const authService = require('../src/services/auth');
const externalApi = require('../stubs/externalApi');
const App = require('../src/models/app');
const winston = require('winston');
const authMiddleware = require('../src/middleware/auth');

// Transporte personalizado para capturar logs
class MemoryTransport extends winston.Transport {
  constructor(options) {
    super(options);
    this.logs = [];
  }

  log(info, callback) {
    this.logs.push(info);
    callback();
  }

  clear() {
    this.logs = [];
  }
}

winston.transports.MemoryTransport = MemoryTransport;

// Mock do authService
jest.mock('../src/services/auth', () => ({
  register: jest.fn(),
  login: jest.fn(),
}));
jest.mock('../stubs/externalApi');
// Mock do middleware auth
jest.mock('../src/middleware/auth', () => jest.fn((req, res, next) => {
  req.user = { id: 'user123' };
  next();
}));
// Mock do modelo App
jest.mock('../src/models/app', () => {
  const mockAppInstance = {
    save: jest.fn().mockResolvedValue(undefined),
    name: '',
  };
  return {
    findOne: jest.fn().mockResolvedValue(null), // Simula que a app não existe
    mockAppInstance, // Para casos onde uma instância é criada
    prototype: {
      save: jest.fn().mockResolvedValue(undefined),
    },
  };
});

describe('API Endpoints - Logging Tests', () => {
  let app;
  let loggerTransport;
  let server; // Para gerenciar o servidor Express

  beforeAll((done) => {
    // Criar um transporte de teste para capturar logs
    loggerTransport = new MemoryTransport();
    logger.clear(); // Limpar transportes padrão
    logger.add(loggerTransport);

    // Configurar o app Express para testes
    app = express();
    app.use(express.json());
    app.use(router);
    server = app.listen(0, done); // Iniciar o servidor em uma porta aleatória
  });

  afterEach(() => {
    jest.clearAllMocks();
    loggerTransport.clear(); // Limpar logs após cada teste
  });

  afterAll((done) => {
    server.close(done); // Fechar o servidor após todos os testes
  });

  describe('POST /register', () => {
    it('deve registrar erro de log quando username está ausente', async () => {
      const response = await request(app)
        .post('/register')
        .send({ password: 'password123' });

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('Username e password são obrigatórios');
      expect(loggerTransport.logs).toContainEqual(
        expect.objectContaining({
          level: 'warn',
          message: 'Tentativa de registro sem username ou password',
        })
      );
    });

    it('deve registrar erro de log quando username é muito curto', async () => {
      const response = await request(app)
        .post('/register')
        .send({ username: 'abc', password: 'password123' });

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('Username precisa de ter entre 5 e 20 caracteres');
      expect(loggerTransport.logs).toContainEqual(
        expect.objectContaining({
          level: 'warn',
          message: expect.stringContaining('Tentativa de registro com username fora dos limites estabelecidos'),
        })
      );
    });

    it('deve registrar sucesso ao criar usuário', async () => {
      authService.register.mockResolvedValue({ id: '123', username: 'testuser' });

      const response = await request(app)
        .post('/register')
        .send({ username: 'testuser', password: 'password123' });

      expect(response.status).toBe(201);
      expect(response.body).toEqual({ id: '123', username: 'testuser' });
      expect(loggerTransport.logs).not.toContainEqual(
        expect.objectContaining({
          level: 'warn',
          message: expect.any(String),
        })
      );
    });

    it('deve registrar erro de log quando authService falha', async () => {
      authService.register.mockRejectedValue(new Error('Usuário já existe'));

      const response = await request(app)
        .post('/register')
        .send({ username: 'testuser', password: 'password123' });

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('Usuário já existe');
      expect(loggerTransport.logs).toContainEqual(
        expect.objectContaining({
          level: 'warn',
          message: 'Erro ao criar utilizador',
        })
      );
    });
  });

  describe('POST /login', () => {
    it('deve registrar erro de log quando password está ausente', async () => {
      const response = await request(app)
        .post('/login')
        .send({ username: 'testuser' });

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('Username e password são obrigatórios');
      expect(loggerTransport.logs).toContainEqual(
        expect.objectContaining({
          level: 'warn',
          message: 'Tentativa de login sem username ou password',
        })
      );
    });

    it('deve registrar sucesso ao fazer login', async () => {
      authService.login.mockResolvedValue({ token: 'mocked-token' });

      const response = await request(app)
        .post('/login')
        .send({ username: 'testuser', password: 'password123' });

      expect(response.status).toBe(200);
      expect(response.body).toEqual({ token: 'mocked-token' });
      expect(loggerTransport.logs).not.toContainEqual(
        expect.objectContaining({
          level: 'warn',
          message: expect.any(String),
        })
      );
    });

    it('deve registrar erro de log quando login falha', async () => {
      authService.login.mockRejectedValue(new Error('Credenciais inválidas'));

      const response = await request(app)
        .post('/login')
        .send({ username: 'testuser', password: 'wrongpassword' });

      expect(response.status).toBe(401);
      expect(response.body.error).toBe('Credenciais inválidas');
      expect(loggerTransport.logs).not.toContainEqual(
        expect.objectContaining({
          level: 'warn',
          message: 'Tentativa de login sem username ou password',
        })
      );
    });
  });

  describe('POST /import/apps', () => {
    it('deve importar apps com sucesso e registrar logs', async () => {
      externalApi.fetchApps.mockResolvedValue([
        { name: 'App Externa 1', externalId: 'ext-001' },
        { name: 'App Externa 2', externalId: 'ext-002' },
      ]);

      // Configurar mocks para App.findOne e App.save
      App.findOne
        .mockResolvedValueOnce(null) // Primeira app não existe
        .mockResolvedValueOnce(null); // Segunda app não existe
      App.mockAppInstance.save.mockResolvedValue(undefined);

      const response = await request(app).post('/import/apps').send();

      expect(response.status).toBe(201);
      expect(response.body.message).toBe('Importação concluída: 2 apps processadas');
      expect(response.body.apps).toHaveLength(2);
      expect(loggerTransport.logs).toContainEqual(
        expect.objectContaining({
          level: 'info',
          message: 'Iniciando importação de 2 apps para o utilizador user123',
        })
      );
      expect(loggerTransport.logs).toContainEqual(
        expect.objectContaining({
          level: 'info',
          message: 'App criada: ext-001 por utilizador user123',
        })
      );
    }, 10000); // Aumentar o timeout para 10 segundos

    it('deve registrar erro ao falhar a importação', async () => {
      externalApi.fetchApps.mockRejectedValue(new Error('Erro na API externa'));

      const response = await request(app).post('/import/apps').send();

      expect(response.status).toBe(500);
      expect(response.body.error).toBe('Erro ao importar apps');
      expect(loggerTransport.logs).toContainEqual(
        expect.objectContaining({
          level: 'error',
          message: 'Erro ao importar apps: Erro na API externa',
        })
      );
    });
  });
});