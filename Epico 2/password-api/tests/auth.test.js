require('dotenv').config();
const request = require('supertest');
const express = require('express');
const mongoose = require('mongoose');
const apiRoutes = require('../src/routes/api');
const User = require('../src/models/user');
const App = require('../src/models/app');
const Password = require('../src/models/password');
const logger = require('../src/utils/logger');

jest.mock('../src/utils/logger', () => ({
  info: jest.fn(),
  warn: jest.fn(),
  error: jest.fn(),
}));

const app = express();
app.use(express.json());
app.use('/api', apiRoutes);

describe('Auth and App API Workflow', () => {
  let token;
  let appid;
  let validUsername = 'TesteWhiteBoxEpico2';
  let validPassword = 'testpassword123';

  beforeAll(async () => {
    try {
      await mongoose.connect(process.env.MONGODB_URI_TEST || process.env.MONGODB_URI, {
        useNewUrlParser: true,
        useUnifiedTopology: true,
        serverSelectionTimeoutMS: 10000,
      });

      const existingUser = await User.findOne({ username: validUsername });
      if (!existingUser) {
        await request(app)
          .post('/api/register')
          .send({
            username: validUsername,
            password: validPassword,
          })
          .expect(201);
      }

      console.log('Conectado ao MongoDB Atlas e utilizador pronto');
    } catch (err) {
      console.error('Erro ao conectar ao MongoDB Atlas:', err);
      throw err;
    }
  }, 30000);

  afterAll(async () => {
    // Limpa os dados apenas após todos os testes
    await User.deleteMany({});
    await App.deleteMany({});
    await Password.deleteMany({});
    await mongoose.connection.close(true);
    await new Promise(resolve => setTimeout(resolve, 1000));
  }, 10000);

  // Teste 1: Tentar criar um usuário com username curto
  // Estrutura condicional testada: if (username.length < 5 || username.length > 20) em src/routes/api.js, linha 20
  // Mapeamento: username='abc' (menor que 5 caracteres, condição verdadeira)
  it('deve falhar ao criar usuário com username curto', async () => {
    const response = await request(app)
      .post('/api/register')
      .send({
        username: 'abc',
        password: validPassword,
      })
      .expect(400);

    expect(response.body).toHaveProperty('error', 'Username precisa de ter entre 5 e 20 caracteres');
    expect(logger.warn).toHaveBeenCalledWith('Tentativa de registro com username fora dos limites estabelecidos: 3');
  }, 10000);

  // Teste: Criar um usuário com sucesso
  // Estrutura condicional testada: if (existingUser) em src/services/auth.js, linha 12
  // Mapeamento: username='TesteWhiteBox4' (novo, não existe no banco, condição falsa)
  it('deve criar um usuário com sucesso quando o username não existe', async () => {
    const user = `${Date.now()}`;
    const response = await request(app)
      .post('/api/register')
      .send({
        username: user,
        password: 'testpassword123',
      })
      .expect(201);
      validUsername = user;
      validPassword = "testpassword123";
    expect(response.body).toHaveProperty('id');
    expect(response.body).toHaveProperty('username', user);
    expect(logger.info).toHaveBeenCalledWith('Utilizador registado: ' + user);
  }, 10000);

  // Teste 3: Tentar fazer login com senha errada
  // Estrutura condicional testada: if (!isMatch) em src/services/auth.js, linha 46
  // Mapeamento: username='testuser' (existe), password='wrongpassword' (incorreta, condição verdadeira)
  it('deve falhar ao fazer login com senha errada', async () => {
    const response = await request(app)
      .post('/api/login')
      .send({
        username: validUsername,
        password: 'wrongpassword',
      })
      .expect(401);

    expect(response.body).toHaveProperty('error', 'Credenciais inválidas');
    expect(logger.warn).toHaveBeenCalledWith('password incorreta para utilizador: ' + validUsername);
  }, 10000);

  // Teste 4: Fazer login com sucesso
  // Estrutura condicional testada: if (!user) em src/services/auth.js, linha 39
  // Mapeamento: username='testuser' (existe no banco, condição falsa), password='testpassword123' (correta)
  it('deve fazer login com sucesso quando as credenciais são válidas', async () => {
    const response = await request(app)
      .post('/api/login')
      .send({
        username: validUsername,
        password: validPassword,
      })
      .expect(200);

    expect(response.body).toHaveProperty('token');
    expect(response.body).toHaveProperty('user');
    expect(response.body.user).toHaveProperty('id');
    expect(response.body.user).toHaveProperty('username', validUsername);
    expect(logger.info).toHaveBeenCalledWith('Utilizador logado: ' + validUsername);
    token = response.body.token; // Armazena o token para os próximos testes
  }, 10000);

  // Teste 5: Criar uma aplicação
  // Estrutura condicional testada: if (!name) em src/routes/api.js, linha 67
  // Mapeamento: name='MinhaAplicacao' (válido, condição falsa)
  it('deve criar uma aplicação com sucesso', async () => {
    const loginResponse = await request(app)
      .post('/api/login')
      .send({
        username: validUsername,
        password: validPassword,
      })
      .expect(200);

    token = loginResponse.body.token;

    const response = await request(app)
      .post('/api/app')
      .set('Authorization', `Bearer ${token}`)
      .send({
        name: 'MinhaAplicacao',
      })
      .expect(201);

    expect(response.body).toHaveProperty('appid');
    expect(response.body).toHaveProperty('name', 'MinhaAplicacao');
    expect(logger.info).toHaveBeenCalledWith(expect.stringContaining('App criado:'));
    appid = response.body.appid; // Armazena o appid para o próximo teste
  }, 10000);

  // Teste 6: Atribuir password à aplicação
  // Estrutura condicional testada: if (!password) em src/routes/api.js, linha 104
  // Mapeamento: password='apppassword123' (válido, condição falsa)
  it('deve atribuir uma password à aplicação com sucesso', async () => {
    const loginResponse = await request(app)
      .post('/api/login')
      .send({
        username: validUsername,
        password: validPassword,
      })
      .expect(200);

    token = loginResponse.body.token;

    const appResponse = await request(app)
      .post('/api/app')
      .set('Authorization', `Bearer ${token}`)
      .send({
        name: 'MinhaAplicacao',
      })
      .expect(201);



    appid = appResponse.body.appid;

    const response = await request(app)
      .post(`/api/password/${appid}`)
      .set('Authorization', `Bearer ${token}`)
      .send({
        password: 'apppassword123',
      })
      .expect(201);

    expect(response.body).toHaveProperty('message', 'password criada');
    expect(response.headers['content-type']).toMatch(/application\/json/);
  }, 10000);
});
