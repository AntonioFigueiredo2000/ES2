// Simula a resposta da API externa
const externalApiStub = {
  async fetchApps() {
    return [
      {
        name: 'App Externa 1',
        externalId: 'ext-001',
      },
      {
        name: 'App Externa 2',
        externalId: 'ext-002',
      },
    ];
  },
};

module.exports = externalApiStub;