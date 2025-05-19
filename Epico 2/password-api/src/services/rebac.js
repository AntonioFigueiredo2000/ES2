const logger = require('../utils/logger');

// Classe para gerir permissões ReBAC
class Ability {
  constructor(user, app) {
    this.user = user;
    this.app = app;
    this.rules = [];

    // Definir permissões com base nas relações
    this.defineRules();
  }

  // Método para definir regras de permissão
  defineRules() {
    // Deny by Default: sem utilizador, nenhuma permissão
    if (!this.user) {
      logger.warn('Tentativa de definir permissões sem utilizador');
      return;
    }

    // Proprietário: permissão total (manage) sobre App e Password
    if (this.app && this.app.owner === this.user.id) {
      this.rules.push({
        action: 'manage',
        subject: 'App',
        conditions: { appid: this.app.appid },
      });
      this.rules.push({
        action: 'manage',
        subject: 'Password',
        conditions: { appid: this.app.appid },
      });
      logger.info(`Permissões de proprietário concedidas para utilizador ${this.user.id} na aplicação ${this.app.appid}`);
    }

    // Editores: podem ler App e ler/atualizar Password
    if (this.app && this.app.editors.includes(this.user.id)) {
      this.rules.push({
        action: 'read',
        subject: 'App',
        conditions: { appid: this.app.appid },
      });
      this.rules.push({
        action: ['read', 'update'],
        subject: 'Password',
        conditions: { appid: this.app.appid },
      });
      logger.info(`Permissões de editor concedidas para utilizador ${this.user.id} na aplicação ${this.app.appid}`);
    }
  }

  // Método para verificar se uma ação é permitida
  can(action, subject, resource) {
    // Deny by Default: sem regras, negar acesso
    if (!this.rules.length) {
      return false;
    }

    // Verificar cada regra
    for (const rule of this.rules) {
      // Verificar se a ação corresponde
      const actions = Array.isArray(rule.action) ? rule.action : [rule.action];
      if (actions.includes(action) || rule.action === 'manage') {
        // Verificar se o sujeito (App ou Password) corresponde
        if (rule.subject === subject) {
          // Verificar condições (ex.: appid)
          if (this.matchConditions(rule.conditions, resource)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  // Método auxiliar para verificar condições
  matchConditions(conditions, resource) {
    if (!conditions || !resource) {
      return true; // Sem condições, assumir correspondência
    }

    for (const key in conditions) {
      if (conditions[key] !== resource[key]) {
        return false;
      }
    }
    return true;
  }
}

// Função exportada para criar uma instância de Ability
const defineAbilitiesFor = (user, app) => {
  return new Ability(user, app);
};

module.exports = { defineAbilitiesFor };